 package monnef.core.asm;
 
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 
 import java.util.ArrayList;
 
 import static monnef.core.MonnefCorePlugin.Log;
 import static org.objectweb.asm.Opcodes.*;
 
 public class RegisterItemMethodVisitor extends MethodVisitor {
     private final boolean debugMessages;
     private ArrayList<Integer> localVars = new ArrayList<Integer>();
    private AdapterLogger logger = new AdapterLogger("[GDH] ");
 
     private enum State {LOOKING, FREEING, PRE_LABEL, PRE_FRAME, ADDING, ADDING_STORE_RESULT, DONE, BROKEN}
 
     private State state = State.LOOKING;
     private int varId_idHint, varId_item, varId_itemId;
 
     public RegisterItemMethodVisitor(int api, MethodVisitor mv, boolean debugMessages) {
         super(api, mv);
         this.debugMessages = debugMessages;
     }
 
     @Override
     public void visitVarInsn(int opcode, int var) {
         if (state == State.DONE) {
             super.visitVarInsn(opcode, var);
         } else if (state == State.ADDING && opcode == ISTORE) {
             newState(State.ADDING_STORE_RESULT);
             logger.log("Setting var id of itemId to " + var + ".");
             varId_itemId = var;
             super.visitVarInsn(opcode, var);
             injectPostHook();
             newState(State.DONE);
         } else {
             if (opcode == ILOAD || opcode == ALOAD) {
                 localVars.add(var);
                 logger.log("added local var " + var);
             }
             super.visitVarInsn(opcode, var);
         }
     }
 
     @Override
     public void visitMethodInsn(int opcode, String owner, String name, String desc) {
         if (state == State.LOOKING && "freeSlot".equals(name)) {
             newState(State.FREEING);
             logger.log("method found freeSlot");
             varId_idHint = localVars.get(1);
             varId_item = localVars.get(2);
             logger.log("Setting var ids: idHint=" + varId_idHint + ", item=" + varId_item);
             super.visitMethodInsn(opcode, owner, name, desc);
         } else if (state == State.PRE_FRAME && "add".equals(name)) {
             newState(State.ADDING);
             super.visitMethodInsn(opcode, owner, name, desc);
         } else if (state == State.LOOKING) {
             localVars.clear();
             logger.log("clearing local vars cache on method call: " + name);
             super.visitMethodInsn(opcode, owner, name, desc);
         } else if (state == State.DONE || state == State.BROKEN) {
             super.visitMethodInsn(opcode, owner, name, desc);
         } else {
             logger.printError("GameData processing broke @ " + state + " in visitMethodInsn(" + name + ")");
             newState(State.BROKEN);
             super.visitMethodInsn(opcode, owner, name, desc);
         }
     }
 
     @Override
     public void visitLabel(Label label) {
         if (state == State.FREEING) {
             newState(State.PRE_LABEL);
             super.visitLabel(label);
         } else {
             super.visitLabel(label);
         }
     }
 
     @Override
     public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
         super.visitFrame(type, nLocal, local, nStack, stack);
         if (state == State.PRE_LABEL) {
             newState(State.PRE_FRAME);
             injectPreHook();
         }
     }
 
     @Override
     public void visitEnd() {
         super.visitEnd();
         if (state != State.DONE) {
             logger.printError("not DONE at the end, code has probably changed so the adapter cannot detect right injection place");
             logger.printAll();
         }
         if (debugMessages) logger.printAll();
     }
 
     private void injectPreHook() {
         logger.log("Injecting preHook");
         mv.visitVarInsn(ALOAD, 0);
         mv.visitVarInsn(ALOAD, varId_item);
         mv.visitVarInsn(ILOAD, varId_idHint);
         // public static int onRegisterItemPre(Item item, int idHint) {
         String signature = "(Lcpw/mods/fml/common/registry/GameData;L" + SrgNames.getSlashedName(SrgNames.C_ITEM.getTranslatedName()) + ";I)I";
         mv.visitMethodInsn(INVOKESTATIC, "monnef/core/asm/GameDataEvents", "onRegisterItemPre", signature);
         mv.visitVarInsn(ISTORE, varId_idHint);
     }
 
     private void injectPostHook() {
         logger.log("Injecting postHook");
         mv.visitVarInsn(ALOAD, 0);
         mv.visitVarInsn(ALOAD, varId_item);
         mv.visitVarInsn(ILOAD, varId_itemId);
         mv.visitVarInsn(ILOAD, varId_idHint);
         // public static void onRegisterItemPost(GameData gameData, Item item, int itemId, int idHint) {
         String signature = "(Lcpw/mods/fml/common/registry/GameData;L" + SrgNames.getSlashedName(SrgNames.C_ITEM.getTranslatedName()) + ";II)V";
         mv.visitMethodInsn(INVOKESTATIC, "monnef/core/asm/GameDataEvents", "onRegisterItemPost", signature);
         logger.print("GameData hooks inserted.");
         CoreTransformer.gameDataHookApplied = true;
     }
 
     private void newState(State newState) {
         if (state != newState) {
             logger.log(String.format("state changed from %s to %s", state, newState));
             state = newState;
         }
     }
 }
