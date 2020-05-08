 package bfrc.java;
 
 import java.io.IOException;
 import java.util.Deque;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import bfrc.ast.AbstractTreeVisitor;
 import bfrc.ast.BlockNode;
 import bfrc.ast.ChangeValueNode;
 import bfrc.ast.MovePointerNode;
 import bfrc.ast.Node;
 import bfrc.ast.Node.NodeType;
 import bfrc.backend.Backend;
 
 public abstract class AbstractJavassistBackend<E extends Exception> extends
 		AbstractTreeVisitor<RuntimeException> implements Backend {
 	
 	private final String className;
 	private StringBuilder body;
 
 	public AbstractJavassistBackend(String className) {
 		this.className = className;
 	}
 
 	@Override
 	public void write(BlockNode ast) throws IOException {
 		ClassPool cp = ClassPool.getDefault();
 		synchronized (cp) {
 			CtClass c = cp.makeClass(className);
 			body = new StringBuilder("public static int main()");
 
 			work(ast);
 			try {
 				CtMethod m = CtNewMethod.make(body.toString(), c);
 				c.addMethod(m);
 				
 				write(c);
 			} catch (CannotCompileException e) {
 				throw new IOException("exception compiling java class", e);
 			} catch (Exception e) {
 				throw new IOException("exception handling compiled class", e);
 			} finally {
 				body = null;
 				c.detach();
 			}
 		}
 	}
 
 	public abstract void write(CtClass clazz) throws E;
 
 	@Override
 	protected void visit(Node node, Deque<BlockNode> stack) {
 		switch (node.type) {
 			case ROOT:
 				body.append("{")
 				    .append("byte[] mem=new byte[10000];")
 				    .append("int ptr=0;");
 				break;
 			case LOOP:
 				body.append("while(mem[ptr]!=0){");
 				break;
 			case VALUE:
 				ChangeValueNode vn = (ChangeValueNode) node;
 				body.append("mem[ptr]+=" + vn.change + ";");
 				break;
 			case POINTER:
 				MovePointerNode pn = (MovePointerNode) node;
 				body.append("ptr+=" + pn.direction + ";");
 				break;
 			case INPUT:
 				body.append("mem[ptr]=System.in.read();");
 				break;
 			case OUTPUT:
				body.append("System.out.print((char)mem[ptr]);");
 				break;
 			default:
 				throw new RuntimeException("unexpected node: " + node.type);
 		}
 	}
 
 	@Override
 	protected void leave(BlockNode node, Deque<BlockNode> stack) {
 		if (node.type == NodeType.ROOT)
 			body.append("return mem[ptr];");
 		body.append("}");
 	}
 }
