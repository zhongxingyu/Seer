 package logic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Clause {
 	private boolean isEmpty;
 	private boolean isFalse;
 	private boolean isTrue;
 	private List<Literal> lista;
 
 	public static Clause emptyClause() {
 		Clause c = new Clause();
 		c.setEmpty();
 		return c;
 	}
 	public Clause(String in) {
 		if(lista== null)
 			lista=new ArrayList<Literal>();
 		int i = 0;
 		int j = 0;
 		while (i > -1 && in.length() > i) {
 			j = in.indexOf('+',i);
 			if (j >= 0) {
 				lista.add(new Literal(in.substring(i, j)));
 				i = j + 1;
 			} else {
 				lista.add(new Literal(in.substring(i)));
 				i = -1;
 			}
 		}
 	}
 	public Clause(Clause kla)
 	{
 		isEmpty=kla.isEmpty();
 		isTrue=kla.isTrue();
 		isFalse=kla.isFalse();
 		lista=new ArrayList<Literal>();
 		for(int i=0;i<kla.getLiterals().size();i++)
 		{
 			lista.add(new Literal(kla.getLiterals().get(i)));
 		}
 	}
 
 	public static Clause trueClause() {
 		Clause c = new Clause();
 		c.setTrue();
 		return c;
 	}
 
 	public static Clause falseClause() {
 		Clause c = new Clause();
 		c.setFalse();
 		return c;
 	}
 
 	public Clause(Literal l) {
 		lista = new ArrayList<Literal>();
 		lista.add(l);
 	}
 
 	public Clause() {
 		lista = new ArrayList<Literal>();
 	}
 
 	public List<Literal> getLiterals() {
 		return lista;
 	}
 
 	public boolean isEmpty() {
 		return isEmpty;
 	}
 
 	private void setEmpty() {
 		isEmpty = true;
 	}
 
 	public boolean isTrue() {
 		return isTrue;
 	}
 
 	private void setTrue() {
 		isTrue = true;
 	}
 
 	public boolean isFalse() {
 		return isFalse;
 	}
 
 	private void setFalse() {
 		isFalse = true;
 	}
 
 	public void add(Literal literal) {
 		lista.add(literal);
 	}
 
 	public List<Literal> asLiterals() {
 		return lista;
 	}
 	public Literal asLiteral() {
 		if(lista.size() != 1)
 			throw new RuntimeException();
 		return lista.get(0);
 	}
 
 	public boolean equals(Object other) {
 		if( !(other instanceof Clause))
 			return false;
 		Clause c = (Clause)other;
 		if(this.getLiterals().size() != c.getLiterals().size())
 			return false;
 		for(Literal i:lista) {
 			if(!c.getLiterals().contains(i))
 				return false;
 		}
 		return true;
 	}
 
 	public Clause getClause(Literal lit) {
 		Clause tmp = new Clause(this);
 		boolean flag=true;
 		for (int i = 0; i < tmp.getLiterals().size(); i++) {
 			if (tmp.getLiterals().get(i).getName() == lit.getName()
 					&& tmp.getLiterals().get(i).getValue() != lit.getValue()) {
 				tmp.getLiterals().remove(i);
 				flag=false;
 			}
 		}
 		if(flag)
 			return null;
 		if(tmp.getLiterals().size()==0)
 			return Clause.emptyClause();
 		return tmp;
 	}
 	/*
 	 * public void check() { boolean check=false; for (int
 	 * i=0;i<lista.size();i++) { check=(check||lista.get(i).getValue()); } if
 	 * (check) { isTrue=true; isFalse=false; } else { isTrue=false;
 	 * isFalse=true; } }
 	 */
 }
