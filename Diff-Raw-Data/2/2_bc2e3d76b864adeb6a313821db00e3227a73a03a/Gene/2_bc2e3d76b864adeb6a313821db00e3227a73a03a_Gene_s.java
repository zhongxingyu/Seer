 /*
 * Copyright (c) 2010 The Jackson Laboratory
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
 package org.jax.drakegenetics.shareddata.client;
 
 import java.util.List;
 
 /**
  *
  * @author gbeane
  */
 public class Gene {
 
     private String symbol;
     private String name;
     private String chromosomeName;
     private double startPosition;
     private int length;
     private List<String> alleles;
 
 
     public Gene()
     {
     }
 
     public void setSymbol(String symbol)
     {
         this.symbol = symbol;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public void setChromosomeName(String chromosomeName)
     {
         this.chromosomeName = chromosomeName;
     }
 
     public void setStartPosition(double startPosition)
     {
         this.startPosition = startPosition;
     }
 
    public void setLenght(int lenght)
     {
         this.length = length;
     }
 
     public void setAlleles(List<String> alleles)
     {
         this.alleles = alleles;
     }
 
     public String getSymbol()
     {
         return symbol;
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getChromosomeName()
     {
         return chromosomeName;
     }
 
     public double getStartPosition()
     {
         return startPosition;
     }
 
     public int getLength()
     {
         return length;
     }
 
     public List<String> getAlleles()
     {
         return alleles;
     }
 }
