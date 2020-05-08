 package de.unisiegen.tpml.graphics.components;
 
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import de.unisiegen.tpml.core.expressions.CurriedLet;
 import de.unisiegen.tpml.core.expressions.CurriedLetRec;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.expressions.Identifier;
 import de.unisiegen.tpml.core.expressions.Lambda;
 import de.unisiegen.tpml.core.expressions.Let;
 import de.unisiegen.tpml.core.expressions.LetRec;
 import de.unisiegen.tpml.core.expressions.MultiLambda;
 import de.unisiegen.tpml.core.expressions.MultiLet;
 import de.unisiegen.tpml.core.expressions.Recursion;
 import de.unisiegen.tpml.core.prettyprinter.PrettyAnnotation;
 import de.unisiegen.tpml.core.prettyprinter.PrettyString;
 import de.unisiegen.tpml.graphics.components.Bound;
 
 public class ShowBound
 {
 static Expression holeExpression;
 	
 
 	private static ShowBound bound = null;
 	private static LinkedList<Bound> tmp = new LinkedList();
 	private boolean debugOutput=false;
 	//public LinkedList<Bound> result = new LinkedList();
 
 	public static ShowBound getInstance()
 	{
 		if (bound == null)
 		{
 			bound = new ShowBound();
 			return bound;
 		}
 		else
 		{
 			
 			return bound;
 		}
 	}
 	
 	
 	public void check(Expression pExpression)
 	{
 	
 		{
 
 			if (pExpression instanceof Lambda)
 			{
 
 				checkLambda((Lambda) pExpression);
 			}
 			else if (pExpression instanceof MultiLambda)
 			{
 
 				checkMultiLambda((MultiLambda) pExpression);
 			}
 			else if (pExpression instanceof MultiLet)
 			{
 
 				checkMultiLet((MultiLet) pExpression);
 			}
 			else if (pExpression instanceof CurriedLetRec)
 			{
 			
 				checkCurriedLetRec((CurriedLetRec) pExpression);
 			}
 			else if (pExpression instanceof CurriedLet)
 			{
 				
 				checkCurriedLet((CurriedLet) pExpression);
 			}
 			else if (pExpression instanceof Recursion)
 			{
 
 				checkRecursion((Recursion) pExpression);
 			}
 			else if (pExpression instanceof Let)
 			{
 
 				checkLet((Let) pExpression);
 			}
 			else
 			{
 				Enumeration<Expression> child = pExpression.children();
 				while (child.hasMoreElements())
 				{
 
 					Expression actualExpression = (Expression) child.nextElement();
 
 					check(actualExpression);
 
 				}
 			}
 
 		}
 
 	}
 	
 
 
 
 	//TODO working end
 	private void checkLambda(Lambda lambda)
 	{
 		// rekursiver Aufruf fr Ausdruck e von lambda
 		Expression e = lambda.getE();
 		check(e);
 		
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
 		Object[] a = lambda.free().toArray();
 		Object[] b = e.free().toArray();
 		
 		LinkedList list = listWithBounds(a, b);
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =lambda.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		
 	checkRec(child,lambda,list);
 	
 
 	}
 	
 	
 	private void checkMultiLambda(MultiLambda lambda)
 	{
 		
 		// rekursiver Aufruf fr Ausdruck e von lambda
 		Expression e = lambda.getE();
 		check(e);
 		
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
 		Object[] a = lambda.free().toArray();
 		Object[] b = e.free().toArray();
 		
 		LinkedList list = listWithBounds(a, b);
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =lambda.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		
 checkRec(child,lambda,list);
 	
 		
 	}
 
 
 	private void checkLet(Let let)
 	{
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
 		Object[] a = new Object[0];
 		Object[] b = let.getE2().free().toArray();
 	
 		check(let.getE1());
 		
 		LinkedList list = listWithBounds(a, b);
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =let.children();
 		tmpChild.nextElement();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		
 		
 		checkRec(child,let,list);
 
 		
 	}
 	
 	private void checkMultiLet(MultiLet let)
 	{
 //	 anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
 		Object[] a = new Object[0];
 		Object[] b = let.getE2().free().toArray();
 	
 		check(let.getE1());
 		
 		LinkedList list = listWithBounds(a, b);
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =let.children();
 		tmpChild.nextElement();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		
 		
 		checkRec(child,let,list);
 
 		
 		
 				
 	}
 	
 
 	
 	
 	private void checkCurriedLet(CurriedLet let)
 	{
 //	 anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
		Object[] b = let.getE1().free().toArray();
 		Object[] a = let.getE2().free().toArray();
 		

 
 		check(let.getE1());
 		check(let.getE2());
 			
		LinkedList list = listWithBounds(new Object[0], b);
		LinkedList list2 = listWithBounds(b, a);
 		
 	
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =let.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		LinkedList<Expression> child2 =new LinkedList();
 		child2.add((child.getLast()));
 		checkRec(child2,let,list2);
 		child.remove(child.size()-1);
 		checkRec(child,let,list);
 		
 		
 
 	}
 	
 	
 	
 
 	private void checkRecursion(Recursion rec)
 	{
 //	 anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
 		Object[] a = rec.free().toArray();
 		Object[] b = rec.getE().free().toArray();
 		
 		LinkedList list = listWithBounds(a, b);
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =rec.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		
 		
 		checkRec(child,rec,list);
 	}
 	
 	
 	private void checkCurriedLetRec(CurriedLetRec rec)
 	{
 //	 anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions von lambda
 		Object[] b = rec.getE1().free().toArray();
 		Object[] a = rec.getE2().free().toArray();
 
 		
 			
 		LinkedList list = listWithBounds(a, b);
 		
 		LinkedList <Expression> child = new LinkedList();
 		Enumeration tmpChild =rec.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression)tmpChild.nextElement());
 		}
 		
 		
 		checkRec(child,rec,list);
 		
 	}
 
 	
 	private Bound checkRec (LinkedList<Expression> child,Expression e, LinkedList <String> list)
 	{
 		Bound result=null;
 		
 		
 		for (int j=0; j<child.size();j++)
 		{
 
 			Expression actualExpression = (Expression) child.get(j);
 			
 
 			if (actualExpression.free().toArray().length > 0)
 			{
 
 				if (actualExpression instanceof Identifier)
 				{
 
 					Identifier id = (Identifier) actualExpression;
 				
 					for (int i = 0; i < list.size(); i++)
 					{
 						
 						if (id.getName().equals(list.get(i)))
 						{
 							
 							PrettyString ps1 = holeExpression.toPrettyString();
 							PrettyAnnotation mark1 = ps1.getAnnotationForPrintable(e);
 							PrettyAnnotation mark2 = ps1.getAnnotationForPrintable(id);
 							
 							
 							
 							
 							int start =getStartOffset(e, id, mark1);
 							
 						
 							
 							
 							int length = start + id.toString().length()-1;
 							if (debugOutput)
 							{
 							System.err.println("Fr den Identifier: Startoffset: " + start
 									+ "Endoffset" + length);
 							System.err.println("Fr die Variable " + id.getName()
 									+ " Startoffset: " + mark2.getStartOffset() + " Endoffset: "
 									+ mark2.getEndOffset());
 							}
 							Bound addToList = new Bound(start,length);
 							addToList.getMarks().add(mark2);
 							tmp.add(addToList);
 					
 							
 							
 					
 							
 							
 							
 						}
 					}
 
 				}
 				else
 				{
 						LinkedList <Expression> childtmp = new LinkedList();
 						Enumeration tmpChild =actualExpression.children();
 						while (tmpChild.hasMoreElements())
 						{
 							child.add((Expression)tmpChild.nextElement());
 						}
 						checkRec(childtmp,e, list);
 				}
 			}
 
 		}
 		
 		return result;
 	}
 
 	
 	
 	public LinkedList<String> listWithBounds ( Object[] a, Object[] b)
 	{
 		LinkedList<String> e1 = new LinkedList();
 		LinkedList<String> e2 = new LinkedList();
 		
 		e1=castArray(a);
 		e2=castArray(b);
 		
 		for (int i = 0; i < e1.size(); i++)
 		{
 			for (int j=0; j<e2.size();j++)
 			{
 				if (e1.get(i).equals(e2.get(j)))
 				{
 					e2.remove(j);
 					j--;
 				}
 			}
 			
 		}
 
 		return e2;
 	}
 	
 
 
 	private LinkedList<String> castArray(Object[] a)
 	{
 		LinkedList<String> tmp = new LinkedList();
 		
 		for (int i = 0; i < a.length; i++)
 		{

 			tmp.add((String) a[i]);
 		}
 		
 		return tmp;
 	}
 	
 	private int getStartOffset(Expression e, Identifier id, PrettyAnnotation mark1)
 	{
 		int start=0;
 		
 		if (e instanceof CurriedLetRec)
 		{
 			CurriedLetRec let= (CurriedLetRec)e;
 			start=8;
 			for (int z=0; z<let.getIdentifiers().length;z++)
 				
 			if (let.getIdentifiers(z).equals(id.toString()))
 			{
 				for (int y=0; y<z;y++)
 				{
 					start+=1+let.getIdentifiers(y).toString().length();
 				}
 			}
 			return start;
 		}
 		else if (e instanceof LetRec)
 		{
 			start = 8;
 			return start;
 		}
 		else if (e instanceof Lambda)
 		{
 			start = mark1.getStartOffset() + 1;
 			return start;
 		}
 		else if (e instanceof Let)
 		{
 			start = mark1.getStartOffset() + 4;
 			return start;
 			
 		}
 		else if (e instanceof MultiLambda)
 		{
 			MultiLambda lambda= (MultiLambda)e;
 			start=2;
 			for (int z=0; z<lambda.getIdentifiers().length;z++)
 				
 			if (lambda.getIdentifiers(z).equals(id.toString()))
 			{
 				for (int y=0; y<z;y++)
 				{
 					start+=2+lambda.getIdentifiers(y).toString().length();
 				}
 			}
 			return start;
 		}
 		
 		else if (e instanceof MultiLet)
 		{
 			MultiLet let= (MultiLet)e;
 			start=5;
 			for (int z=0; z<let.getIdentifiers().length;z++)
 				
 			if (let.getIdentifiers(z).equals(id.toString()))
 			{
 				for (int y=0; y<z;y++)
 				{
 					start+=2+let.getIdentifiers(y).toString().length();
 				}
 			}
 			return start;
 		}
 		else if (e instanceof CurriedLet)
 		{
 			CurriedLet let= (CurriedLet)e;
 			start=4;
 			for (int z=0; z<let.getIdentifiers().length;z++)
 				
 			if (let.getIdentifiers(z).equals(id.toString()))
 			{
 				for (int y=0; y<z;y++)
 				{
 					start+=1+let.getIdentifiers(y).toString().length();
 				}
 			}
 			return start;
 		}
 		else if (e instanceof Recursion)
 		{
 			start=4;
 			return start;
 		}
 		return start;
 	}
 	
 		
 	
 
 	public static void setHoleExpression(Expression holeExpression)
 	{
 		bound.holeExpression = holeExpression;
 		tmp=new LinkedList();
 	}
 	
 	public LinkedList getAnnotations()
 	{
 		return tmp;
 	}
 	
 /** often needed to Debug
  		System.out.println("a");
 		for (int i=0;i<a.length;i++)
 		{
 			System.out.println(a[i]);
 		}
 		
 		System.out.println("b");
 		for (int i=0;i<b.length;i++)
 		{
 			System.out.println(b[i]);
 		}
  */
 }
