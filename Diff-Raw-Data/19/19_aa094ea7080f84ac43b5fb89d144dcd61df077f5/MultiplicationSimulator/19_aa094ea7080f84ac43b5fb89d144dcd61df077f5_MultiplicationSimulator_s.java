 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2012-2013  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.simulator;
 
 import guma.simulator.AbstractSimulator;
 import java.util.Arrays;
 import guma.arithmetic.Praxis;
 
 public class MultiplicationSimulator extends AbstractSimulator
 {
 
 	/**
 	*Array List that stores intermediate results
 	*/
 	private byte[][] endiamesa=null;
 
 	/**
 	*Stores for each intermediate result where the result will be stored
 	*/
 	private int[] endiamesaLastDigit=null;
 
 	/**
 	*Shows what intermediate result will be chosen 
 	*/
 	private int endiamesoApotelesmaIndex=0;
 	
 	/**
 	*When 2 numbers when multiplying have zeros in the end then we ignore them and we apend them into the final result to the end
 	*/
 	private int apendZeros=0;
 	
 	/**
 	*Variable that shows if needed to display a message for apending zeros
 	*/
 	private boolean apendMessage=false;
 	
 	/**
 	*This variable shows if the intermediate results will be added directly or not
 	*/
 	private boolean spartial=true;
 	
 	/**
 	*Constructor Method
 	*@param telestis1: the first operator of the number that we will simulate the first operation
 	*@param telestis2: the second operator of the number that we will simulate the second operation
 	*@param spartial: shows if the results will be added directly or not
 	*/
 	public MultiplicationSimulator(int telestis1,int telestis2, boolean spartial)
 	{
 		super(telestis1,telestis2);
 		type=Praxis.MULTIPLICATION;
 		this.spartial=spartial;
 		
 		endiamesa=new byte[this.telestis2.length][];
 		endiamesaLastDigit=new int[this.telestis2.length];
 
 		//initilizing the matrix for storing the intermediate results
 		for(int i=0;i<endiamesa.length;i++)
 		{
 			endiamesa[i]=new byte[this.telestis1.length+i+1];
 			endiamesaLastDigit[i]=endiamesa[i].length-1;
 			Arrays.fill(endiamesa[i],(byte)0);
 		}
 		
 		//setting the first intermediate result
 		endiamesoApotelesmaIndex=0;
 
 		//When one of the operators have zero digits on the end we ignore them and we apennd them to the final result		
 		while(telestis1Index>=0 && telestis2Index>=0 && (this.telestis1[telestis1Index]==0 || this.telestis2[telestis2Index]==0))
 		{
 			if(telestis1Index>=0 && this.telestis1[telestis1Index]==0)
 			{
 				telestis1Index--;
 				apendZeros++;
 			}
 			
 			if( telestis2Index>=0 && this.telestis2[telestis2Index]==0)
 			{
 				telestis2Index--;
 				apendZeros++;
 			}
 			
 		}
 		
 		if(apendZeros>0)
 		{
 			apendMessage=true;
 		}
 		
 		
 	}
 	
 	public MultiplicationSimulator(int telestis1,int telestis2)
 	{
 		this(telestis1,telestis2,false);
 	}
 
 	/**
 	*This Method does the next step of an arithmetic praxis Simulation
 	*Returns true if it has next step to do
 	*/	
 	public boolean next()
 	{
 		message="";
 		
 		
 		if(apendMessage)
 		{
 			message="Ανγωούμε τα 0 που έχουν στο τέλος οι αριθμοί και εκτελούμε την πράξη του πολλαπλασιαμού από τα μη μηδενικα στοιχεία";
 			apendMessage=false;
 			return true;
 		}
 		
 		if(telestis2Index>=0)
 		{
 			if(telestis1Index>=0)
 			{
 				message="Πολλαπλασιάζουμε τα ψηφία "+telestis2[telestis2Index]+"*"+telestis1[telestis1Index];
 				temp=telestis1[telestis1Index]*telestis2[telestis2Index];
 				message+=" και ο πολαπλασιαμός έχει αποτέλεσμα: "+temp;
 				if(kratoumeno!=0)
 				{
					message+="\n Επειδή από την προηγούμενη πράξη έχουμε κρατούμενο το προσθέτουμε στο αποτέλεσμα";					
 					temp+=kratoumeno;
 				}
 
 				byte tempM[]=AbstractSimulator.seperateDigits((int)temp);
 				
 				if(tempM.length>1)
 				{
					message+="\n Η πράξη μας είχε διψήφιο αποτέλεσμα άρα το πρώτο ψηφίο το κρατάμε κρατούμενο";
 					kratoumeno=tempM[0];
 					endiamesa[endiamesoApotelesmaIndex][endiamesaLastDigit[endiamesoApotelesmaIndex]]=tempM[tempM.length-1];
 				}
 				else
 				{
 					kratoumeno=0;
 					endiamesa[endiamesoApotelesmaIndex][endiamesaLastDigit[endiamesoApotelesmaIndex]]=(byte)temp;
 					
 				}
								
 				endiamesaLastDigit[endiamesoApotelesmaIndex]--;
 				telestis1Index--;
 				return true;	
 			}
 			else
 			{
 				telestis2Index--;
 				telestis1Index=telestis1.length-1;
 				
 				if(kratoumeno>0)
 				{
 				 	message+="Βάζουμε το κρατούμενο στην αρχή του αποτελέσματος";
				 	endiamesa[endiamesoApotelesmaIndex][endiamesaLastDigit[endiamesoApotelesmaIndex]]=kratoumeno;
 				 	kratoumeno=0;
 				}
 				
 				if(telestis2Index>=0 && endiamesoApotelesmaIndex<endiamesaLastDigit.length)
 				{
 					message+="Βάζουμε ένα 0 κάτω από το "+endiamesa[endiamesoApotelesmaIndex][endiamesa[endiamesoApotelesmaIndex].length-1];
 					
 					endiamesoApotelesmaIndex++;
 					endiamesa[endiamesoApotelesmaIndex][endiamesa[endiamesoApotelesmaIndex].length-1]=(byte)0;
					endiamesaLastDigit[endiamesoApotelesmaIndex]=endiamesa[endiamesoApotelesmaIndex].length-endiamesoApotelesmaIndex;
					
 				}
 				return true;
 			}
 
 		}
 		else
 		{
 			temp=0;
 			message="Προσθαίτουμε τα ενδιάμεσα αποτελέσματα.\n";
 			
 				for(int i=0;i<endiamesa.length;i++)
 				{
 					int temp1=(int)mergeDigits(endiamesa[i]);
 					temp+=temp1;
 				}
 				temp*=(int)Math.pow(10,apendZeros);
 			
 				result=seperateDigits(temp);
 			
 			message+="Τέλος προσομοίωσης";
 			return false;
 		}
 	}
 	
 	/**
 	*@override
 	*/
 	public String toString(boolean html)
 	{
 		String s="";
 		if(html)
 		{
			s+="<table><tr>"+getTelestis1("<td>","</td>","<td><font color=\"blue\">","</font></td>")
 							+"</tr><tr><td>"+ type+"</td>"+getTelestis2("<td>","</td>","<td><font color=\"blue\">","</font></td>")+
 							"</tr></table><hr><br><table>";
 			for(int i=0;i<=endiamesoApotelesmaIndex;i++)
 			{
 				s+="<tr>";
 				for(int j=0;j<(endiamesa.length-i);j++)
 				{
 					s+="<td>\t</td>";
 				}
 				s+=getTelestis(endiamesa[i],"<td>","</td>",endiamesaLastDigit[i],"<td><font color=\"blue\">","</font></td>");
 				s+="</tr>";
 			}
 			s+="</table><hr>"+"<table><tr><td>"+getResult()+"</td></tr></table>";
 		}
 		else
 		{
 			s+=""+getTelestis1("\t","")+"\n"+ type+getTelestis2("\t","")+"\n";
 			
 			for(int i=0;i<endiamesoApotelesmaIndex;i++)
 			{
 				for(int j=0;j<(endiamesa.length-i);j++)
 				{
 					s+="\t";
 				}
 				s+=getTelestis(endiamesa[i],"\t","")+"\n";
 			}
 			s+="\n";
 		}
 		return s;
 	}		
 }
