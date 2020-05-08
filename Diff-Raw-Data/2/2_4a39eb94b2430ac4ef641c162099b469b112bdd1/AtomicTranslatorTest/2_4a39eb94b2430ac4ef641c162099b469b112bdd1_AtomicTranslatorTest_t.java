 /******************************************************************************
   Event trace translator
   Copyright (C) 2012 Sylvain Halle
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 import java.io.*;
 import java.util.Vector;
 
 import ca.uqac.info.ltl.Operator;
 import ca.uqac.info.trace.conversion.*;
 import ca.uqac.info.trace.*;
 
 /**
  * A test for the AtomicTranslator. (May eventually be deleted.)
  * @author sylvain
  *
  */
 public class AtomicTranslatorTest
 {
 	public static void main(String[] args)
 	{
 		XmlTraceReader xtr = new XmlTraceReader();
 		File f = new File("traces/trace1.xml");
 		Operator o = null;
 		try
 		{
			o = Operator.parseFromString("G (((a) = (1)) | ((b) = (1)))");
 		}
 		catch (Operator.ParseException e)
 		{
 			System.exit(1);
 		}
 		EventTrace t = xtr.parseEventTrace(f);
 		AtomicTranslator at = new AtomicTranslator();
 		Vector<String> params = new Vector<String>();
 		params.add("a"); params.add("b");
 		at.setParameters(params);
 		String out = at.translateTrace(t);
 		System.out.println(out);
 		out = at.translateFormula(o);
 		System.out.println(out);
 	}
 	
 }
