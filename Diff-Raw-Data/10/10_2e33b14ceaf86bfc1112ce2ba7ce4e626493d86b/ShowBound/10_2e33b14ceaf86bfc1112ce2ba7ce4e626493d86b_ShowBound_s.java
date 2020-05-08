 package de.unisiegen.tpml.graphics.components;
 
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import de.unisiegen.tpml.core.expressions.Application;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.expressions.Identifier;
 import de.unisiegen.tpml.core.expressions.Lambda;
 import de.unisiegen.tpml.core.expressions.Let;
 import de.unisiegen.tpml.core.expressions.MultiLambda;
 import de.unisiegen.tpml.core.prettyprinter.PrettyAnnotation;
 import de.unisiegen.tpml.core.prettyprinter.PrettyString;
 
 public class ShowBound
 {
 
 	
 	static Expression holeExpression;
 	
 	private static ShowBound bound = null;
 	private static LinkedList<Bound> tmp = new LinkedList();
 	public LinkedList<Bound> result = new LinkedList();
 
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
 
 				checkBoundLambda((Lambda) pExpression);
 			}
 			else if (pExpression instanceof Let)
 			{
 
 				checkBoundLet((Let) pExpression);
 			}
 			else if (pExpression instanceof MultiLambda)
 			{
 
 				checkBoundMultiLambda((MultiLambda) pExpression);
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
 
 	public void checkBoundLambda(Lambda pLambda)
 	{
 		
 		Expression e = pLambda.getE();
 
 		check(e);
 
 		Object[] a = pLambda.free().toArray();
 		Object[] b = e.free().toArray();
 
 		System.out.println(pLambda.toString());
 		checkBound(pLambda, a, b);
 		
 	}
 
 	public void checkBoundLet(Let pLet)
 	{
 		Object[] a = pLet.getE1().free().toArray();
 		Object[] b = pLet.getE2().free().toArray();
 		
 		//System.out.println(pLet.toString());
 		//System.out.println(pLet.getE1().toString());
 		//System.out.println(pLet.getE2().toString());
 		checkBound(pLet,a,b);
 	}
 	
 	public void checkBoundMultiLambda(MultiLambda pMultiLambda)
 	{
 		Expression e = pMultiLambda.getE();
 
 		check(e);
 
 		Object[] a = pMultiLambda.free().toArray();
 		Object[] b = e.free().toArray();
 		
 		checkBound(pMultiLambda,a,b);
 	}
 
 	public void checkBound (Expression pE, Object[] a, Object[] b)
 	{
 		LinkedList<String> e1 = new LinkedList();
 		LinkedList<String> e2 = new LinkedList();
 		
 		for (int i = 0; i < a.length; i++)
 		{
 
 			e1.add((String) a[i]);
 		}
 		for (int j = 0; j < b.length; j++)
 		{
 
 			e2.add((String) b[j]);
 		}
 
 		for (int i = 0; i < e1.size(); i++)
 		{
 			if (e2.contains(e1.get(i)))
 			{
 				e2.remove(i);
 				i--;
 			}
 		}
 
 		childCheck(pE.children(), e2, pE);
 
 	}
 	
 	public void childCheck(Enumeration child, LinkedList e2, Expression pE)
 	{
 			
 
 		while (child.hasMoreElements())
 		{
 
 			Expression actualExpression = (Expression) child.nextElement();
 
 			if (actualExpression.free().toArray().length > 0)
 			{
 
 				if (actualExpression instanceof Identifier)
 				{
 
 					Identifier id = (Identifier) actualExpression;
 				
 					for (int i = 0; i < e2.size(); i++)
 					{
 						
 						if (id.getName().equals(e2.get(i)))
 						{
 							
 							PrettyString ps1 = holeExpression.toPrettyString();
 							PrettyAnnotation mark1 = ps1.getAnnotationForPrintable(pE);
 							PrettyAnnotation mark2 = ps1.getAnnotationForPrintable(id);
 							System.out.println("Identifier:"+pE.toString());
 							System.out.println("Var:"+id.toString());
 							
 							
 							
 							int start =0;
 							if (holeExpression instanceof Lambda)
 							{
 								start = mark1.getStartOffset() + 1;
 								
 							}
 							else 
 								if (holeExpression instanceof Let)
 							{
 								start = mark1.getStartOffset() + 4;
 							}
 							
 							int length = start + id.toString().length()-1;
 
 							System.err.println("Fr den Identifier: Startoffset: " + start
 									+ "Endoffset" + length);
 							System.err.println("Fr die Variable " + id.getName()
 									+ " Startoffset: " + mark2.getStartOffset() + " Endoffset: "
 									+ mark2.getEndOffset());
 							Bound addToList = new Bound(start,length);
 							addToList.getMarks().add(mark2);
 							
 						}
 					}
 
 				}
 				else
 					childCheck(actualExpression.children(), e2, pE);
 			}
 
 		}
 	}
 	
 
 	public LinkedList getAnnotations()
 	{
 		boolean found=false;
		
 		
 		for (int i=0; i<tmp.size();i=i+2)
 		{
 			found=false;
 			for (int j=0; j<result.size();j++ )
 			{
 				if (tmp.get(i).getStartOffset()== result.get(j).getStartOffset())
 				{
 					result.get(j).getMarks().addAll(tmp.get(i).getMarks());
 					found=true;
 				}
 			}
 			if (!found)
 			{
 				Bound listIt = new Bound(tmp.get(i).getStartOffset(),tmp.get(i).getEndOffset());
 				listIt.getMarks().addAll(tmp.get(i).getMarks());
 				result.add(listIt);
 				
 			}
 		}
 		System.out.println("Array: "+result.size());
 		return result;
 	}
 
 	public static void setHoleExpression(Expression holeExpression)
 	{
 		ShowBound.holeExpression = holeExpression;
 		tmp=new LinkedList();
 	}
 }
 
 
