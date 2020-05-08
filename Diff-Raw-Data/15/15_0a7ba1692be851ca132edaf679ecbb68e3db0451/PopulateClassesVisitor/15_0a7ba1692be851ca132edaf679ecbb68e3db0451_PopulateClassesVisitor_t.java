 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import syntaxtree.ArrayType;
 import syntaxtree.BooleanType;
 import syntaxtree.ClassDeclaration;
 import syntaxtree.ClassExtendsDeclaration;
 import syntaxtree.Goal;
 import syntaxtree.Identifier;
 import syntaxtree.IntegerType;
 import syntaxtree.MethodDeclaration;
 import syntaxtree.Node;
 import syntaxtree.NodeList;
 import syntaxtree.NodeListOptional;
 import syntaxtree.NodeOptional;
 import syntaxtree.NodeSequence;
 import syntaxtree.Type;
 import syntaxtree.TypeDeclaration;
 import syntaxtree.VarDeclaration;
 import visitor.GJDepthFirst;
 
 
 public class PopulateClassesVisitor extends GJDepthFirst<Object, Object> {
 
 	public PopulateClassesVisitor() {
 		super();
 	}
 	
 	//
 	// Auto class visitors--probably don't need to be overridden.
 	//
 	public Object visit(NodeList n, Object argu) 
 	{
 		List<Object> _ret=new ArrayList<Object>();
 		int _count=0;
 		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) 
 		{
 			Object o = e.nextElement().accept(this,argu);
 			_ret.add(o);
 			_count++;
 		}
 		return _ret;
 	}
 
 	   public Object visit(NodeListOptional n, Object argu) {
 		   List<Object> _ret = new ArrayList<Object>();
 	      if ( n.present() ) {
 	         int _count=0;
 	         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
 	            Object o = e.nextElement().accept(this,argu);
 	            _ret.add(o);
 	            _count++;
 	         }
 	      }
 	      
 	      return _ret;
 	   }
 
 	   public Object visit(NodeOptional n, Object argu) {
 	      if ( n.present() )
 	         return n.node.accept(this,argu);
 	      else
 	         return null;
 	   }
 
 	   public Object visit(NodeSequence n, Object argu) {
 	      Object _ret=null;
 	      int _count=0;
 	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
 	         e.nextElement().accept(this,argu);
 	         _count++;
 	      }
 	      return _ret;
 	   }
 
 	/**
 	* f0 -> MainClass()
 	* f1 -> ( TypeDeclaration() )*
 	* f2 -> <EOF>
 	*/
 	public Object visit(Goal n, Object argu) 
 	{
 	    List<MJClass> classList= (List<MJClass>) n.f1.accept(this, argu);
 	    Map<String, MJClass> classes = new HashMap<String, MJClass>();	    
 	    for (MJClass mjclass : classList)
 	    {
 	    	classes.put(mjclass.name, mjclass);
 	    }
 	    
 	    return classes;
 	}
 	
 	   /**
 	    * f0 -> ClassDeclaration()
 	    *       | ClassExtendsDeclaration()
 	    */
 	   public Object visit(TypeDeclaration n, Object argu) {
 	      MJClass _ret = (MJClass) n.f0.accept(this, argu);
 	      return _ret;
 	   }
 
 	/**
 	* f0 -> "class"
 	* f1 -> Identifier()
 	* f2 -> "{"
 	* f3 -> ( VarDeclaration() )*
 	* f4 -> ( MethodDeclaration() )*
 	* f5 -> "}"
 	*/
 	public Object visit(ClassDeclaration n, Object argu) 
 	{	    
 		MJClass _ret = visitClass(n.f1, null, n.f3, n.f4, argu);
 		return _ret;
 	}
 
 	/**
 	* f0 -> "class"
 	* f1 -> Identifier()
 	* f2 -> "extends"
 	* f3 -> Identifier()
 	* f4 -> "{"
 	* f5 -> ( VarDeclaration() )*
 	* f6 -> ( MethodDeclaration() )*
 	* f7 -> "}"
 	*/
 	public Object visit(ClassExtendsDeclaration n, Object argu) 
 	{
 	    MJClass _ret = visitClass(n.f1, n.f3, n.f5, n.f6, argu);
 	    return _ret;
 	}
 	
 	private MJClass visitClass(Identifier classNameNode, Identifier parentNameNode, NodeListOptional fieldsNode, NodeListOptional methodsNode, Object argu)
 	{
 		String name = classNameNode.f0.tokenImage;
 		MJClass parentClass = null;
 		if (parentNameNode != null)
 		{
 			String parentName = parentNameNode.f0.tokenImage;
 			parentClass = new MJClass(null, null, parentName, null);
 		}
 		
 	    List<MJField> fieldList = (List<MJField>) fieldsNode.accept(this, argu);
 	    Map<String, MJField> fieldMap = new HashMap<String, MJField>();
 	    for (MJField mjfield : fieldList)
 	    {
 	    	fieldMap.put(mjfield.name, mjfield);
 	    }
 	    
 	    List<MJMethod> methodList = (List<MJMethod>) methodsNode.accept(this, argu);
 	    Map<String, MJMethod> methodMap = new HashMap<String, MJMethod>();
 	    for (MJMethod mjmethod : methodList)
 	    {
 	    	methodMap.put(mjmethod.name, mjmethod);
 	    }
 	    
 		MJClass mjclass = new MJClass(methodMap, fieldMap, name, parentClass);
 		return mjclass;
 	}
 	
 	   /**
 	    * f0 -> "public"
 	    * f1 -> Type()
 	    * f2 -> Identifier()
 	    * f3 -> "("
 	    * f4 -> ( FormalParameterList() )?
 	    * f5 -> ")"
 	    * f6 -> "{"
 	    * f7 -> ( VarDeclaration() )*
 	    * f8 -> ( Statement() )*
 	    * f9 -> "return"
 	    * f10 -> Expression()
 	    * f11 -> ";"
 	    * f12 -> "}"
 	    */
 	   public Object visit(MethodDeclaration n, Object argu) {
 	      String returnType = (String) n.f1.accept(this, argu);
 	      String name = n.f2.f0.tokenImage;
 	      
 	      MJMethod _ret = new MJMethod(name, null, returnType, -1);
 	      return _ret;
 	   }	
 	
 	   /**
 	    * f0 -> Type()
 	    * f1 -> Identifier()
 	    * f2 -> ";"
 	    */
 	   public Object visit(VarDeclaration n, Object argu) {
 	      String type = (String) n.f0.accept(this, argu);
 	      String name = n.f1.f0.tokenImage;
 	      
 	      MJField _ret = new MJField(type, name, -1);
 	      return _ret;
 	   }
 	   
 	   /**
 	    * f0 -> ArrayType()
 	    *       | BooleanType()
 	    *       | IntegerType()
 	    *       | Identifier()
 	    */
 	   public Object visit(Type n, Object argu) {
 	      String _ret = (String) n.f0.accept(this, argu);
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "int"
 	    * f1 -> "["
 	    * f2 -> "]"
 	    */
 	   public Object visit(ArrayType n, Object argu) {
 	      String _ret = "int[]";
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "boolean"
 	    */
 	   public Object visit(BooleanType n, Object argu) {
 	      String _ret = "boolean";
 	      return _ret;
 	   }
 
 	   /**
 	    * f0 -> "int"
 	    */
 	   public Object visit(IntegerType n, Object argu) {
 		   String _ret = "int";
 	      return _ret;
 	   }
	   
	   /**
	    * f0 -> <IDENTIFIER>
	    */
	   public Object visit(Identifier n, Object argu) {
	      String _ret = n.f0.tokenImage;
	      return _ret;
	   }
 }
