 package ch.unibe.iam.scg.rewriter;
 
import ch.unibe.iam.scg.Mapper;
 import ch.unibe.iam.scg.rewriter.helper.ArrayInterceptor;
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CodeConverter;
 import javassist.CtBehavior;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.CtMember;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 import javassist.bytecode.AccessFlag;
 import javassist.expr.ExprEditor;
 import javassist.expr.FieldAccess;
 import javassist.expr.NewArray;
 import javassist.expr.NewExpr;
 
 public class InterceptAccessorsRewriter implements ClassRewriter {
 	public void rewrite(CtClass ctClass) throws CannotCompileException
 	{
 		if( ctClass.isInterface() ) return;
 		
 		System.out.println( "-> Rewire accesses for "+ ctClass.getName() );
 		
 		 for (final CtBehavior ctMethod : ctClass.getDeclaredBehaviors()) {
         	System.out.println( "Method:" + ctMethod.getName());
             ExprEditor exprEditor = new InterceptAccessorsEditor( ctMethod );
         	ctMethod.instrument( exprEditor );
         }
 		 //@TODO uncomment
 		 
 //		 try {
 //			 CodeConverter conv = new CodeConverter();
 //			 CtClass indirectionClass = ClassPool.getDefault().get(ArrayInterceptor.class.getName());
 //			 conv.replaceArrayAccess( indirectionClass, new CodeConverter.DefaultArrayAccessReplacementMethodNames());
 //			 ctClass.instrument(conv);
 //		} catch (NotFoundException e) {
 //			throw new CannotCompileException(e);
 //		} catch (CannotCompileException e) {
 //			throw e;
 //		}
 		 
 		 System.out.println( "<- Rewired accesses for "+ ctClass.getName() );
 	}
 }
 
 class InterceptAccessorsEditor extends ExprEditor {
 	
 	final CtBehavior ctMethod;
 	
 	public InterceptAccessorsEditor( CtBehavior method )
 	{
 		this.ctMethod = method;
 	}
 	
 	
 	private boolean needsRewrite( String className, String fieldName ) {
		return Mapper.needsRewrite(className) && ! fieldName.equals( ClassRewriter.CONTEXT_INFO );
 	}
 	
 	 private boolean isProperty(String name) {
 	        return (name.startsWith( ClassRewriter.GETTER_PREFIX ) || 
 	        		name.startsWith( ClassRewriter.SETTER_PREFIX )) && name.length() > 3;
 	  }
 	 
 	 private String propertyOfAccessor(String accessor) {
 		 String propertyName = ctMethod.getName().substring(ClassRewriter.GETTER_PREFIX.length());
          propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
          return propertyName;
 	 }
 	 
 	private boolean classIsSubclassOf( CtClass subclass, CtClass superclass ) {
 		return subclass.equals(superclass) || subclass.subclassOf( superclass );
 	}
 	
 	@Override
 	public void edit(NewArray a) throws CannotCompileException {
 		// Storing contextInfo at position 0 won't work because of types
 		// a.replace("$_ = $proceed($1+1);");
 		a.replace( "$_ = ($r) "+ ArrayInterceptor.class.getName()+".registerArray( $proceed($$) ); ");
 	}
 	
     @Override
     public void edit(FieldAccess fieldAccess) throws CannotCompileException {
     	System.out.println( "Field:" + fieldAccess.getFieldName());
         try {
 
         	String fieldName = fieldAccess.getFieldName();
         	String fieldClassName = fieldAccess.getField().getDeclaringClass().getName();
         	CtClass fieldClass = fieldAccess.getField().getDeclaringClass();
         	
         	if( ( fieldAccess.getField().getModifiers() & AccessFlag.FINAL ) > 0 )
         	{
         		return;
         	}
         	
         	if( needsRewrite( fieldClassName, fieldName ) ) {
         	
                 if ( classIsSubclassOf( ctMethod.getDeclaringClass(), fieldClass ) ) {
                     if ( isProperty( ctMethod.getName() )) {
                     	String property = propertyOfAccessor( ctMethod.getName() );
                     	if (property.toLowerCase().equals(fieldName.toLowerCase())) {
                     		// We do not rewrite field access xxx in 
                     		// getXxx or setXxx as it would lead to recursion.
                     		return;
                     	}
                     }
                 }
 
 
             	String propertyName = 
 						fieldName.substring(0, 1).toUpperCase()
 						+ fieldName.substring(1);
 				String getter = ClassRewriter.GETTER_PREFIX + propertyName;
 				String setter = ClassRewriter.SETTER_PREFIX + propertyName;
 				
 				
                if (fieldAccess.isReader()) {
             	   	fieldAccess.replace("$_ = $0." + getter + "();");
                } else if (fieldAccess.isWriter()) {
             	   	fieldAccess.replace( "$0." + setter+"($1);");
                }
             }
 
         } catch (Exception e) {
 			throw new CannotCompileException("Error rewritting field accesses", e);
         }
     }
 }
