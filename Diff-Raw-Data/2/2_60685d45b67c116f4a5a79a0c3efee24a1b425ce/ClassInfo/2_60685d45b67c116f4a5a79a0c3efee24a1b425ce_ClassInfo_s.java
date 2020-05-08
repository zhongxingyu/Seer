 package symbol;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Enumeration;
 
 import temp.Label;
 import semant.Env;
 
 public class ClassInfo
 {
 	public Label vtable;
 	
 	public Symbol name;
 	
 	public ClassInfo base;
 
 	public Hashtable<Symbol, VarInfo> attributes;
 	public Hashtable<Symbol, MethodInfo> methods;
 	public Hashtable<Symbol, MethodInfo> methodsByName;
 	
 	private HashSet<Symbol> attributesNames;
 	private HashSet<Symbol> methodsNames;
 	
 	public Vector<Symbol> attributesOrder;
 	public Vector<Symbol> vtableIndex;
 	
 	public ClassInfo(Symbol n)
 	{
 		this(n,null);
 	}
 	
 	// metodo que serve SOMENTE para 'copiar' os atributos da classe base
 	// para a classe derivada, bem como para criar a vtable.
 	// CUIDADO: CHAMAR ESTE METODO SOMENTE UMA VEZ!
 	public void setBase(ClassInfo base)
 	{
 		int i;
 		// parte facil: 'copiar' os campos da classe pai.
 		for( i = base.attributesOrder.size() - 1; i >= 0 ; i-- )
 		{
 			// coloca o dito cujo no lugar certo
 			Symbol s = base.attributesOrder.get(i);
 			this.attributesOrder.insertElementAt(s, 0);
 			
 			// coloca este atributo hash de campos
 			// somente se nao ha campo declarado
 			// com o mesmo nome na classe derivada.
 			if ( !attributes.containsKey(s) )
 				attributes.put(s, base.attributes.get(s));
 		}
 		
 		// parte muito chata: criar a vtable
 		Vector<Symbol> vtableClone = (Vector<Symbol>)base.vtableIndex.clone();
 		Hashtable<Symbol, MethodInfo> methodTableClone = 
 			(Hashtable<Symbol, MethodInfo>)base.methods.clone();
 		HashSet<Symbol> methodsNameClone = (HashSet<Symbol>) base.methodsNames.clone();
 		
 		for( i = 0; i < vtableIndex.size(); i++ )       
 		{
 			// coloca na vtable somente os metodos que nao
 			// sao herdados.
 			Symbol name = vtableIndex.get(i);
 			
 			if ( !base.methodsNames.contains( name ) )
 			{
 				vtableClone.add(name);
 				methodsNameClone.add(name);
 				
 				MethodInfo m = methods.get(name);
 			}
 			
 			methodTableClone.put(name, methods.get(name));
 		}
 		
 		vtableIndex = vtableClone;
 		methods = methodTableClone;
 		methodsNames = methodsNameClone;
 		
 		this.base = base;
 	}
 	
 	public ClassInfo(Symbol n, ClassInfo b)
 	{
 		super();
 		
 		name = n;
 		base = b;
 		
 		attributes = new Hashtable<Symbol, VarInfo>();
 		methods = new Hashtable<Symbol, MethodInfo>();
 		methodsByName = new Hashtable<Symbol, MethodInfo>();
 		
 		attributesNames = new HashSet<Symbol>();
 		methodsNames = new HashSet<Symbol>();
 		
 		attributesOrder = new Vector<Symbol>();
 		
 		vtableIndex = new Vector<Symbol>();
 	}
 
 	public boolean checkCyclicInherit()
 	{
 		Vector<Symbol> inherited = new Vector<Symbol>();
 		for( ClassInfo i = this; i != null; i = i.base )
 		{
 			for( int check = 0; check < inherited.size(); check++ )
 				if (inherited.get(check) == i.name)
 					return false;
 
 			inherited.add(i.name);
 		}
 		return true;
 
 	}
 	
 	public boolean addAttribute(VarInfo var)
 	{
 		if ( attributesNames.contains(var.name) )
 			return false;
 
 		attributes.put(var.name, var);
 		attributesNames.add(var.name);
 		
 		attributesOrder.add(var.name);
 		
 		return true;
 	}
 	
 	public int getAttributeOffset(Symbol name)
 	{
 		if ( !attributesNames.contains(name) )
 			return -1;
 		
 		// pega a ultima declaracao do simbolo.
 		return attributesOrder.lastIndexOf(name) + 1; // adicionando 1 para considerar a vtable
 	}
 	
 	public int getMethodOffset(Symbol name)
 	{
 		if ( !methodsNames.contains(name) )
 			return -1;
 		
 		return vtableIndex.indexOf(name);
 	}
 	
 	public boolean addMethod(MethodInfo method)
 	{
 		// using decoration allow us to have polymorphism
 		Symbol methodName = Symbol.symbol(method.decorateName());
 
 		if ( methodsNames.contains(methodName) )
 			return false;
 		
 		methods.put(methodName, method);
 		methodsNames.add(methodName);
 		
 		vtableIndex.add(methodName);
 		
 		return true;
 	}
 
 	public boolean removeMethod(MethodInfo method)
 	{
 		// using decoration allow us to have polymorphism
 		Symbol methodName = Symbol.symbol(method.decorateName());
 
 		if ( ! methodsNames.contains(methodName) )
 			return false;
 		
 		methods.remove(methodName);
 		methodsNames.remove(methodName);
 		
 		vtableIndex.remove(methodName);
 		
 		return true;
 	}
 
 	public void checkOverLoading(Env e)
 	{
 		Enumeration<MethodInfo> mInfo = methods.elements();
 		MethodInfo actualMethodInfo;
 		MethodInfo checkMethodInfo;
 		for (Enumeration<MethodInfo> m = mInfo; m.hasMoreElements() ;) 
 		{
 			actualMethodInfo = m.nextElement();
 			if (methodsByName.containsKey(actualMethodInfo.name))
 			{
 				checkMethodInfo = methodsByName.get(actualMethodInfo.name);
				if (checkMethodInfo.type == actualMethodInfo.type && checkMethodInfo.getFormalsString() == actualMethodInfo.getFormalsString())
 				{
 					if (checkMethodInfo.parent == name)
 					{
 						removeMethod(actualMethodInfo);
 						methodsByName.put(checkMethodInfo.name, checkMethodInfo);
 					}
 					else
 					{
 						removeMethod(checkMethodInfo);
 						methodsByName.put(actualMethodInfo.name, actualMethodInfo);
 					}
 				}
 				else
 				{
 					if (getMethodOffset(Symbol.symbol(checkMethodInfo.decorateName())) < getMethodOffset(Symbol.symbol(actualMethodInfo.decorateName())))
 					{
 						checkMethodInfo = actualMethodInfo;
 						removeMethod(actualMethodInfo);
 					}
 					else
 					{
 						methodsByName.put(actualMethodInfo.name, actualMethodInfo);
 						removeMethod(checkMethodInfo);
 					}
 					e.err.Print(new Object[]{
 						"[" + checkMethodInfo.type.line + "," + checkMethodInfo.type.row + "] " +
 						"Functions overloading not allowed, removing: " + checkMethodInfo.type +
 						" " + checkMethodInfo.name + "(" + checkMethodInfo.getFormalsString() + ")"});
 				}
 				continue;
 			}
 			methodsByName.put(actualMethodInfo.name, actualMethodInfo);
 		}
 	}
 }
