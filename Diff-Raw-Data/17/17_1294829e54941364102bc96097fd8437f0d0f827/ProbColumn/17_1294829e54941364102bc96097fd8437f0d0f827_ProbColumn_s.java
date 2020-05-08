 /** 
  * Author:  anthony.fodor@gmail.com    
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 package probabilisticNW;
 
 public class ProbColumn
 {
 	private static final int A_INDEX =0;
 	private static final int C_INDEX =1;
 	private static final int G_INDEX =2;
 	private static final int T_INDEX =3;
 	private static final int GAP_INDEX =4;
 	private double counts[] = new double[5];
 	
 	private double totalNum =0;
 	private double distance =0;
 	
 	public double getDistance()
 	{
 		return distance;
 	}
 	
 	public double getScoreDiag(ProbColumn other, double match, double mismatch)
 	{
 		double score =0;
 		
 		
 		for (int x=0; x<= 3; x++)
 			score += this.counts[x] * other.counts[x] * match;
 		
 		for( int x=0; x <=2; x++)
 			for( int y=x+1; y <=3; y++)
 				if( x != y)
 					score += this.counts[x] * other.counts[y] * mismatch;
 		
 		return score;
 	}
 	
 	/*
 	 * Makes and returns a new column based on a merge of this column and 
 	 * the otherColumn
 	 */
 	public ProbColumn merge( ProbColumn otherColumn)
 	{
 		ProbColumn pc = new ProbColumn();
 		pc.totalNum = this.totalNum + otherColumn.totalNum;
 		for( int x=0; x < 5; x++)
 		{
 			pc.counts[x] = this.counts[x] + otherColumn.counts[x];
 		}
 		
 		for( int x=0; x < 5; x++)
 			for (int y= 0; y < 5; y++)
 				if( x != y )
 					pc.distance += (this.counts[x]/this.totalNum) * (otherColumn.counts[y]/otherColumn.totalNum);
 		
 		return pc;
 	}
 	
 	public double getFractionA()
 	{
 		return counts[A_INDEX] / totalNum;
 	}
 	
 	public double getFractionC()
 	{
 		return counts[C_INDEX]  / totalNum;
 	}
 	
 	public double getFractionG()
 	{
 		return counts[G_INDEX]  / totalNum;
 	}
 	
 	public double getFractionT()
 	{
 		return counts[T_INDEX]  / totalNum;
 	}
 	
 
 	public double getFractionGap()
 	{
 		return counts[GAP_INDEX] / totalNum;
 	}
 	
 	public double getTotalNum()
 	{
 		return totalNum;
 	}
 	
 	public ProbColumn()
 	{
 		
 	}
 	
 	public ProbColumn(char c)
 	{
 		this.addChar(c);
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "["+ getFractionA() + "," + getFractionC() + "," + getFractionG() + "," + getFractionT() + "," + 
 						getFractionGap() + "]";
 	}
 	
 	/*
 	 * Non A,C,G,T and - are ignored
 	 */
 	public void addChar(char c)
 	{
 		totalNum++;
 		
 		if( c == 'A')
 			counts[A_INDEX]++;
 		else if ( c == 'C')
 			counts[C_INDEX]++;
 		else if ( c == 'G')
 			counts[G_INDEX]++;
 		else if ( c == 'T')
 			counts[T_INDEX]++;
 		else if ( c == '-')
 			counts[GAP_INDEX]++;
 	}
 	
 	
 }
