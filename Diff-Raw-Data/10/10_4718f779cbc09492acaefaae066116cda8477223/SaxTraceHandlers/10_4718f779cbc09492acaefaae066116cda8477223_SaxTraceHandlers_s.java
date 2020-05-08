 /*
     LTL trace validation using MapReduce
     Copyright (C) 2012 Sylvain Hallé
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  The Sax Trace Handlers was created by Maxime Soucy-Boivin Copyright (C) 2013
 */
 
 package ca.uqac.dim.mapreduce.ltl;
 
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 public class SaxTraceHandlers extends DefaultHandler
 {
 	    //List who contains the variable for the current Event
 		private List<VariableConditions> listVarCond;
 		//The current variable
 		private VariableConditions VarCond;
 		//Flags who help to know where the parsing is
 		private boolean inTrace, inEvent, inVar;
 		//Buffer who help to get the informations
 		private StringBuffer buffer;
 		private Set<Atom> atoms;
 		
 		//Pointer of the TraceCollector to help to send the LTLTuple to it
 		private XmlSaxTraceCollector ptrTraceCol = null;
 
 		/**
 	    * Create an instance of SaxTraceHandlers with the pointer 
 	    * of the TraceCollector and the Set of Atoms needed to analyse
 	    */
 		public SaxTraceHandlers( XmlSaxTraceCollector ptr, Set<Atom> Setatoms)
 		{
 			super();
 			ptrTraceCol = ptr;
 			atoms = Setatoms;
 		}
 		
 		/**
 		* Set the value of the position for the variable inTrace
 		* @param value can take true of false
 		*/
 	    public void setInTrace(boolean value)
 		{
 			inTrace = value;
 		}
 			
 		/**
 		* Returns the value of the variable that contains inTrace at the precise moment when this function is called
 		* @return true or false
 		*/
 		public boolean getInTrace()
 		{
 			return inTrace;
 		}
 			
 		/**
 		* Set the value of the position for the variable inEvent
 		* @param value can take true of false
 		*/
 		public void setInEvent(boolean value)
 		{
 			inEvent = value;
 		}
 			
 		/**
 		* Returns the value of the variable that contains inEvent at the precise moment when this function is called
 		* @return true or false
 		*/
 		public boolean getInEvent()
 		{
 			return inEvent;
 		}
 			
 		/**
 		* Set the value of the position for the variable inVar
 		* @param value can take true of false
 		*/
 		public void setInVar(boolean value)
 		{
 			inVar = value;
 		}
 			
 		/**
 		* Returns the value of the variable that contains inVar at the precise moment when this function is called
 		* @return true or false
 		*/
 		public boolean getInVar()
 		{
 			return inVar;
 		}
 		
 		/**
 		* Detects the opening of each tag in the file and perform the corresponding treatment
 		*/
 		public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException
 		{
 			if(qName.equals("Trace"))
 			{
 				setInTrace(true);
 			}
 			else if(qName.equals("Event"))
 			{
 				listVarCond = new LinkedList<VariableConditions>();
 				setInEvent(true);
 			}
 			else 
 			{
 				buffer = new StringBuffer();
 				if(qName.contains("p"))
 				{
 					setInVar(true);
 					VarCond = new VariableConditions();
 				}
 				else
 				{
 					//Throw an error
 					throw new SAXException("Unknown Tag : "+qName);
 				}
 			}
 		}
 		
 		/**
 		* Detects the closing of each tag in the file and perform the corresponding treatment
 		*/
 		public void endElement(String uri, String localName, String qName)throws SAXException
 		{
 			if(qName.equals("Trace"))
 			{
 				setInTrace(false);
 			}
 			else if(qName.equals("Event"))
 			{
 				//Function who do the treatment and create the LTLTuple if needed
 				parse();
 				
 				//Update the depth of the trace
 				ptrTraceCol.m_traceLength++;
 				
 				//Erase the current Event, because the treatment is done
 				VarCond = null;
 				listVarCond = null;
 				setInEvent(false);
 			}
 			else if(qName.contains("p"))
 			{
 				//Add the values of the variable into the object who represent it
 				VarCond.setVarName(qName);
 				VarCond.addValueVar(buffer.toString());
 				//Add the variable into the list who represent the event
 				listVarCond.add(VarCond);
 				buffer = null;
 				setInVar(false);
 			}
 			else
 			{
 				//Throw an error
 				throw new SAXException("Unknown Tag : "+qName);
 			}          
 		}
 		
 		/**
 		* Function for detecting the characters to be treated
 		*/
 		public void characters(char[] ch,int start, int length)throws SAXException
 		{
 			String read = new String(ch,start,length);
 			if(buffer != null) buffer.append(read);       
 		}
 		
 		/**
 		* Start of the Sax Parsing 
 		*/
 		public void startDocument() throws SAXException 
 		{
 			System.out.println("Sax parser use for parsing the xml file");
 		}
 		
 		/**
 		* End of the Sax Parsing 
 		*/
 		public void endDocument() throws SAXException 
 		{
 			System.out.println("End of the Sax parsing");
 		}
 		
 		/**
 		* Function who do the treatment and create the LTLTuple if needed
 		*/
 		private void parse()
 		{
 			int sizeList = 0;
 			sizeList = listVarCond.size();
 			
 			//Variable for the Done variable
 			List<String> listVarDone = new LinkedList<String>();
 			int lengthVarDone = listVarDone.size();
 			boolean VarDone = false;
 			int pos = 0;
 			
 			//Allows to treat all variables
 			for (int i = 0; i < sizeList; i++)
 			{
 				//Gets the current variable to treat and its informations
 				VariableConditions varTemp = new VariableConditions();
 				varTemp = listVarCond.get(i);
 				String nameVar = varTemp.getVarName();
 				String value = varTemp.getVarValue(0);
 				
 				//Compares each atoms to the variable
 				for (Atom a : atoms)
 				{
 					String[] m_parts = null;
 					String name = a.toString();
 					name = name.substring(1, name.length() - 1);
 					m_parts = name.split("/");
 					
 					//Add only the variables corresponding to the atom
 					if((m_parts[0].equals(nameVar)) && (m_parts[1].equals(value)))
 					{
 						pos = 0;
 						lengthVarDone = listVarDone.size();
 						VarDone = false;
 						
 						//Checks if the variable has already been processed
 						while(pos < lengthVarDone)
 						{
 							//If it's the case
							if(listVarDone.get(pos).equals(nameVar))
 							{
 								pos = lengthVarDone;
 								VarDone = true;
 							}
 						}
 						
 						//Adds the variable only once to the TraceCollector
 						if(VarDone != true)
 						{
 							LTLTupleValue v = new LTLTupleValue(a, ptrTraceCol.getTraceLength(), 0);
 							LTLTuple t = new LTLTuple(a, v);
 							ptrTraceCol.collect(t);
							listVarDone.add(nameVar);
 						}//if VarDone
 					}// if
 				}// for Atom
 			}//for (sizelist) listVarCond
 		}
 }
