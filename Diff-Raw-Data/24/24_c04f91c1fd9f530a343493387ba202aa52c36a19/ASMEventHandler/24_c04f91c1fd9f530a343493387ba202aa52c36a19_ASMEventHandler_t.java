 package com.nexus.event;
 
import java.io.File;
import java.io.FileOutputStream;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 
 import com.google.common.collect.Maps;
 
 public class ASMEventHandler implements IEventListener, Opcodes{
 	
 	private static int IDs = 0;
 	private static final String HANDLER_DESC = Type.getInternalName(IEventListener.class);
 	private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(IEventListener.class.getDeclaredMethods()[0]);
 	private static final ASMClassLoader LOADER = new ASMClassLoader();
 	private static final HashMap<Method, Class<?>> cache = Maps.newHashMap();
 	
	private static final boolean SAVE_GENERATED_CLASSES = false;
	private static final File OUTPUT_DIR = new File("generated");
	
 	private final IEventListener handler;
 	private final EventListener subInfo;
 	
 	public ASMEventHandler(Object target, Method method) throws Exception{
		handler = (IEventListener) createWrapper(method).getConstructor(Object.class).newInstance(target);
 		subInfo = method.getAnnotation(EventListener.class);
 	}
 	
 	@Override
 	public void invoke(Event event){
 		if(handler != null){
 			if(!event.isCancelable() || !event.isCanceled() || subInfo.receiveCanceled()){
 				try{
 					handler.invoke(event);
 				}catch(ClassCastException e){}
 			}
 		}
 	}
 	
 	public EventPriority getPriority(){
 		return subInfo.priority();
 	}
 	
 	public Class<?> createWrapper(Method callback){
 		if(cache.containsKey(callback)){
 			return cache.get(callback);
 		}
 		ClassWriter cw = new ClassWriter(0);
 		MethodVisitor mv;
 		
 		String name = getUniqueName(callback);
 		String desc = name.replace('.', '/');
 		String instType = Type.getInternalName(callback.getDeclaringClass());
 		String eventType = Type.getInternalName(callback.getParameterTypes()[0]);
 		
 		/*Add:
 		 *	import com.nexus.event.events.PlaylistEvent.Open;
 		 *	import com.nexus.playlist.PlaylistManager;
 		 *	
 		 *	public class ASMEventHandler_0_PlaylistManager_OnOpenPlaylist_Open implements IEventListener{
 		 *		
 		 *		public PlaylistManager instance;
 		 *	
 		 *		public ASMEventHandler_0_PlaylistManager_OnOpenPlaylist_Open(PlaylistManager paramObject){
 		 *			instance = paramObject;
 		 *		}
 		 *		
 		 *		public void invoke(Event paramEvent){
 		 *			instance.OnOpenPlaylist((PlaylistEvent.Open)paramEvent);
 		 *		}
 		 *	}
 		 */
 		
 		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, "java/lang/Object", new String[]{HANDLER_DESC});
 		
 		cw.visitSource("NEXUS-EVENTBUS", null);
 		{
 			cw.visitField(ACC_PUBLIC, "instance", "L" + instType + ";", null, null).visitEnd();
 		}
 		
 		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
 			mv.visitCode();
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, instType);
 			mv.visitFieldInsn(PUTFIELD, desc, "instance", "L" + instType + ";");
 			mv.visitInsn(RETURN);
 			mv.visitMaxs(2, 2);
 			mv.visitEnd();
 		}
 		
 		{
 			mv = cw.visitMethod(ACC_PUBLIC, "invoke", HANDLER_FUNC_DESC, null, null);
 			mv.visitCode();
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, desc, "instance", "L" + instType + ";");
 			mv.visitVarInsn(ALOAD, 1);
 			mv.visitTypeInsn(CHECKCAST, eventType);
 			mv.visitMethodInsn(INVOKEVIRTUAL, instType, callback.getName(), Type.getMethodDescriptor(callback));
 			mv.visitInsn(RETURN);
 			mv.visitMaxs(2, 2);
 			mv.visitEnd();
 		}
 		cw.visitEnd();
 		
		if(SAVE_GENERATED_CLASSES){
			if(!OUTPUT_DIR.exists()) OUTPUT_DIR.mkdir();
			try{
				FileOutputStream fos = new FileOutputStream(new File(OUTPUT_DIR, name + ".class"));
				fos.write(cw.toByteArray());
				fos.close();
			}catch(Exception e){}
		}
		
 		Class<?> cl = LOADER.define(name, cw.toByteArray());
 		cache.put(callback, cl);
 		return cl;
 	}
 	
 	private String getUniqueName(Method callback){
 		return String.format("com.nexus.event.dynamic.EventHandlerBridge_%d_%s_%s_%s", IDs++, callback.getDeclaringClass().getSimpleName(), callback.getName(), callback.getParameterTypes()[0].getSimpleName());
 	}
 	
 	private static class ASMClassLoader extends ClassLoader{
 		
 		private ASMClassLoader(){
 			super(ASMClassLoader.class.getClassLoader());
 		}
 		
 		public Class<?> define(String name, byte[] data){
 			return defineClass(name, data, 0, data.length);
 		}
 	}
 }
