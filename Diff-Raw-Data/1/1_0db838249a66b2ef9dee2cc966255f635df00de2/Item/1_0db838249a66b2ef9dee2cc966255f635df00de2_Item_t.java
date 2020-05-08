 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.lpg;
 
 import java.io.IOException;
 import org.vesalainen.grammar.GRule;
 import org.vesalainen.grammar.Symbol;
 import org.vesalainen.grammar.GTerminal;
 import java.util.List;
 import java.util.Set;
 import org.vesalainen.parser.util.HtmlPrinter;
 import org.vesalainen.parser.util.Numerable;
 
 /**
  *
  * @author tkv
  */
 public class Item implements Comparable<Item>, Numerable
 {
     private GRule rule;
     private Symbol symbol;
     private int dot;
     private int number;
     private List<Symbol> suffix;
     private Set<GTerminal> firstSet;
 
     public Item(int number, GRule rule, Symbol symbol, int dot)
     {
         this.number = number;
         this.rule = rule;
         this.symbol = symbol;
         this.dot = dot;
     }
 
     public Item predessor(int distance)
     {
         return rule.predessor(this, distance);
     }
 
     public Item predessor()
     {
         return rule.predessor(this);
     }
 
     public Item next()
     {
         return rule.next(this);
     }
 
     public void setFirstSet(Set<GTerminal> firstSet)
     {
         this.firstSet = firstSet;
     }
 
     public Set<GTerminal> getFirstSet()
     {
         return firstSet;
     }
 
     public List<Symbol> getSuffix()
     {
         return suffix;
     }
 
     public void setSuffix(List<Symbol> suffix)
     {
         this.suffix = suffix;
     }
 
     public int getNumber()
     {
         return number;
     }
 
     public GRule getRule()
     {
         return rule;
     }
 
     public boolean isFinal()
     {
         return symbol.isEmpty();
     }
 
     public Symbol getSymbol()
     {
         return symbol;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (obj == null)
         {
             return false;
         }
         if (getClass() != obj.getClass())
         {
             return false;
         }
         final Item other = (Item) obj;
         if (this.rule != other.rule && (this.rule == null || !this.rule.equals(other.rule)))
         {
             return false;
         }
         if (this.dot != other.dot)
         {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 7;
         hash = 89 * hash + (this.rule != null ? this.rule.hashCode() : 0);
         hash = 89 * hash + this.dot;
         return hash;
     }
 
   @Override
     public String toString()
     {
         StringBuilder sb = new StringBuilder();
         sb.append(number).append(":");
         sb.append(rule.getLeft());
         sb.append("->");
         int index = 0;
         for (Symbol r : rule.getRight())
         {
             if (index == dot)
             {
                 sb.append(".");
             }
             sb.append(r.toString());
             sb.append(' ');
             index++;
         }
         if (index == dot)
         {
             sb.append(".");
         }
         return sb.toString();
     }
 
     public int getDot()
     {
         return dot;
     }
 
     public int compareTo(Item o)
     {
         return number - o.number;
     }
 
     void print(HtmlPrinter p) throws IOException
     {
         p.linkSource("#rule"+rule.getNumber(), rule.getLeft().toString());
         p.append("->");
         int index = 0;
         for (Symbol r : rule.getRight())
         {
             if (index == dot)
             {
                 p.append(".");
             }
             r.print(p);
             p.append(' ');
             index++;
         }
         if (index == dot)
         {
             p.append(".");
         }
     }
 
     public void print(Appendable p) throws IOException
     {
        p.append(rule.getLeft().toString());
         p.append("->");
         int index = 0;
         for (Symbol r : rule.getRight())
         {
             if (index == dot)
             {
                 p.append(".");
             }
             r.print(p);
             p.append(' ');
             index++;
         }
         if (index == dot)
         {
             p.append(".");
         }
     }
 
 }
