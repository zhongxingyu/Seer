 package de.unisiegen.tpml.graphics.components;
 
 import java.util.Enumeration;
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
 
 public class ShowBound
 {
 	static Expression holeExpression;
 
 	private static ShowBound bound = null;
 	private static LinkedList<Bound> tmp = new LinkedList<Bound>();
 	private boolean debug = false;
 	
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
 				checkChild(pExpression);
 				
 			}
 
 		}
 
 	}
 
 	private void checkChild(Expression pExpression)
 	{
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = pExpression.children();
 		while (tmpChild.hasMoreElements())
 		{
 
 			child.add((Expression) tmpChild.nextElement());
 
 		}
 
 		{
 			for (int i = 0; i < child.size(); i++)
 
 			{
 				check(child.get(i));
 
 				if (debug)
 				{
 					System.out.print(child.get(i).toPrettyString());
 
 					try
 					{
 						PrettyString ps = holeExpression.toPrettyString();
 						PrettyAnnotation mark = ps.getAnnotationForPrintable(child
 								.get(i));
 						System.out.println("start:" + mark.getStartOffset());
 					}
 					catch (Exception e)
 					{
 						System.out.println("Kein Startoffset fr diesen Ausdruck verfgbar!");
 					}
 				}
 			}
 		}
 		
 	}
 
 	private void checkLambda(Lambda lambda)
 	{
 		// rekursiver Aufruf fr Ausdruck e von lambda
 		Expression e = lambda.getE();
 		checkChild(lambda);
 
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		// von lambda
 		Object[] a = lambda.free().toArray();
 		Object[] b = e.free().toArray();
 
 		LinkedList<String> list = listWithBounds(a, b);
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = lambda.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 
 		checkRec(child, lambda, list);
 
 	}
 
 	private void checkMultiLambda(MultiLambda lambda)
 	{
 
 		// rekursiver Aufruf fr Ausdruck e von lambda
 		Expression e = lambda.getE();
 		checkChild(lambda);
 
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		// von lambda
 		Object[] a = lambda.free().toArray();
 		Object[] b = e.free().toArray();
 
 		LinkedList<String> list = listWithBounds(a, b);
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = lambda.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 
 		checkRec(child, lambda, list);
 
 	}
 
 	private void checkLet(Let let)
 	{
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		// von lambda
		
 		Object[] b = let.getE2().free().toArray();
		Object[] c = let.free().toArray();
		
		if (debug)

		{
			System.out.println(let.toString());
		
			System.out.println("b");
			for (int i = 0; i < b.length; i++)
			{
				System.out.println(b[i]);
			}
			System.out.println("c");
			for (int i = 0; i < c.length; i++)
			{
				System.out.println(c[i]);
			}

		}
 
 		checkChild(let);
 
		LinkedList<String> list = listWithBounds(c, b);
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = let.children();
 		tmpChild.nextElement();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 
 		checkRec(child, let, list);
 
 	}
 
 	private void checkMultiLet(MultiLet let)
 	{
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		// von lambda
 		Object[] a = let.free().toArray();
 		Object[] b = let.getE2().free().toArray();
 
 		checkChild(let);
 		LinkedList<String> list = listWithBounds(new Object[b.length], b);
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = let.children();
 		tmpChild.nextElement();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 
 		checkRec(child, let, list);
 
 	}
 
 	private void checkCurriedLet(CurriedLet let)
 	{
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		Object[] c = let.getE2().free().toArray();
 		Object[] b = let.getE1().free().toArray();
 		Object[] a = let.free().toArray();
 
 		checkChild(let);
 		
 		if (debug)
 
 		{
 			System.out.println("a");
 			for (int i = 0; i < a.length; i++)
 			{
 				System.out.println(a[i]);
 			}
 			System.out.println("b");
 			for (int i = 0; i < b.length; i++)
 			{
 				System.out.println(b[i]);
 			}
 			System.out.println("c");
 			for (int i = 0; i < c.length; i++)
 			{
 				System.out.println(c[i]);
 			}
 
 		}
 		LinkedList<String> list = listWithBounds(new Object[a.length], a);
 		LinkedList<String> list2 = listWithBounds(c, b);
 		list2.add(let.getIdentifiers(0));
 
 		if (list.contains(let.getIdentifiers(0)))
 		{
 			for (int i = 0; i < list.size(); i++)
 			{
 				if (list.get(i).equals(let.getIdentifiers(0)))
 				{
 					list.remove(i);
 					i--;
 				}
 			}
 		}
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = let.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 		LinkedList<Expression> child2 = new LinkedList<Expression>();
 
 		child2.add((child.getLast()));
 		checkRec(child2, let, list2);
 		child.remove(child.size() - 1);
 		checkRec(child, let, list);
 
 	}
 
 	private void checkRecursion(Recursion rec)
 	{
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		// von lambda
 		Object[] a = rec.free().toArray();
 		Object[] b = rec.getE().free().toArray();
 
 		checkChild(rec);
 
 		LinkedList<String> list = listWithBounds(a, b);
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = rec.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 
 		checkRec(child, rec, list);
 	}
 
 	private void checkCurriedLetRec(CurriedLetRec rec)
 	{
 		// anlegen von Arrays mit den frei vorkommenden Namen der beiden expressions
 		Object[] a = rec.getE2().free().toArray();
 		Object[] b = rec.getE1().free().toArray();
 		Object[] c = rec.free().toArray();
 
 		checkChild(rec);
 
 		LinkedList<String> list = listWithBounds(c, a);
 		LinkedList<String> list2 = listWithBounds(c, b);
 
 		LinkedList<Expression> child = new LinkedList<Expression>();
 		Enumeration tmpChild = rec.children();
 		while (tmpChild.hasMoreElements())
 		{
 			child.add((Expression) tmpChild.nextElement());
 		}
 		LinkedList<Expression> child2 = new LinkedList<Expression>();
 		child2.add((child.getLast()));
 		checkRec(child2, rec, list);
 		child.remove(child.size() - 1);
 		checkRec(child, rec, list2);
 
 	}
 
 	private Bound checkRec(LinkedList<Expression> child, Expression e,
 			LinkedList<String> list)
 	{
 		Bound result = null;
 
 		for (int j = 0; j < child.size(); j++)
 		{
 
 			Expression actualExpression = child.get(j);
 
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
 
 							int start = getStartOffset(e, id, mark1);
 
 							int length = start + id.toString().length() - 1;
 
 							//TODO old debug output
 							if (false)
 							{
 								System.err.println("Fr den Identifier: Startoffset: " + start
 										+ "Endoffset" + length);
 								System.err.println("Fr die Variable " + id.getName()
 										+ " Startoffset: " + mark2.getStartOffset()
 										+ " Endoffset: " + mark2.getEndOffset());
 							}
 							boolean exists = false;
 							for (int k = 0; k < tmp.size(); k++)
 							{
 								if (start == tmp.get(k).getStartOffset())
 								{
 									exists = true;
 									tmp.get(k).getMarks().add(mark2);
 									tmp.get(k).getExpressions().add(e);
 								}
 							}
 							if (!exists)
 							{
 								Bound addToList = new Bound(start, length);
 								addToList.getMarks().add(mark2);
 								addToList.getExpressions().add(e);
 								tmp.add(addToList);
 							}
 
 						}
 					}
 
 				}
 				else
 				{
 					LinkedList<Expression> childtmp = new LinkedList<Expression>();
 					Enumeration tmpChild = actualExpression.children();
 					while (tmpChild.hasMoreElements())
 					{
 						child.add((Expression) tmpChild.nextElement());
 					}
 					checkRec(childtmp, e, list);
 				}
 			}
 
 		}
 
 		return result;
 	}
 
 	public LinkedList<String> listWithBounds(Object[] a, Object[] b)
 	{
 		LinkedList<String> e1 = new LinkedList<String>();
 		LinkedList<String> e2 = new LinkedList<String>();
 
 		e1 = castArray(a);
 		e2 = castArray(b);
 
 		for (int i = 0; i < e1.size(); i++)
 		{
 			for (int j = 0; j < e2.size(); j++)
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
 		LinkedList<String> tmp = new LinkedList<String>();
 
 		for (int i = 0; i < a.length; i++)
 		{
 			if (a[i] != null)
 				tmp.add((String) a[i]);
 		}
 
 		return tmp;
 	}
 
 	private int getStartOffset(Expression e, Identifier id, PrettyAnnotation mark1)
 	{
 		int start = 0;
 
 		if (e instanceof CurriedLetRec)
 		{
 			CurriedLetRec let = (CurriedLetRec) e;
 			start = mark1.getStartOffset() + 8;
 			for (int z = 0; z < let.getIdentifiers().length; z++)
 
 				if (let.getIdentifiers(z).equals(id.toString()))
 				{
 					for (int y = 0; y < z; y++)
 					{
 						start += 1 + let.getIdentifiers(y).toString().length();
 					}
 				}
 			return start;
 		}
 		else if (e instanceof LetRec)
 		{
 			start = mark1.getStartOffset() + 8;
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
 			MultiLambda lambda = (MultiLambda) e;
 			start = 2 + mark1.getStartOffset();
 			for (int z = 0; z < lambda.getIdentifiers().length; z++)
 
 				if (lambda.getIdentifiers(z).equals(id.toString()))
 				{
 					for (int y = 0; y < z; y++)
 					{
 						start += 2 + lambda.getIdentifiers(y).toString().length();
 					}
 				}
 			return start;
 		}
 
 		else if (e instanceof MultiLet)
 		{
 			MultiLet let = (MultiLet) e;
 			start = mark1.getStartOffset() + 5;
 			for (int z = 0; z < let.getIdentifiers().length; z++)
 
 				if (let.getIdentifiers(z).equals(id.toString()))
 				{
 					for (int y = 0; y < z; y++)
 					{
 						start += 2 + let.getIdentifiers(y).toString().length();
 					}
 				}
 			return start;
 		}
 		else if (e instanceof CurriedLet)
 		{
 			CurriedLet let = (CurriedLet) e;
 			start = mark1.getStartOffset() + 4;
 			for (int z = 0; z < let.getIdentifiers().length; z++)
 
 				if (let.getIdentifiers(z).equals(id.toString()))
 				{
 					for (int y = 0; y < z; y++)
 					{
 						start += 1 + let.getIdentifiers(y).toString().length();
 					}
 				}
 			return start;
 		}
 		else if (e instanceof Recursion)
 		{
 			start = mark1.getStartOffset() + 4;
 			return start;
 		}
 		return start;
 	}
 
 	public static void setHoleExpression(Expression holeExpression)
 	{
 		ShowBound.holeExpression = holeExpression;
 
 		tmp = new LinkedList<Bound>();
 	}
 
 	public LinkedList getAnnotations()
 	{
 		return tmp;
 	}
 
 	/**
 	 * often needed to Debug System.out.println("a"); for (int i=0;i<a.length;i++) {
 	 * System.out.println(a[i]); }
 	 * 
 	 * System.out.println("b"); for (int i=0;i<b.length;i++) {
 	 * System.out.println(b[i]); }
 	 */
 }
