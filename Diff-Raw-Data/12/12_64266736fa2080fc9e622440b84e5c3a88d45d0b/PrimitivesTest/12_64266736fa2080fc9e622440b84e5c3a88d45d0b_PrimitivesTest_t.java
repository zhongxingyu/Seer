 /*
  * Copyright 2011 Matthew Avery, mavery@advancedpwr.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.advancedpwr.record.methods;
 
 import com.advancedpwr.record.AbstractRecorderTest;
 import com.advancedpwr.record.examples.LangObjects;
 import com.advancedpwr.record.examples.Person;
 import com.advancedpwr.record.examples.Primitives;
 
 public class PrimitivesTest extends AbstractRecorderTest
 {
 
 	protected void setUp()
 	{
 		super.setUp();
 	}
 
 	public void testPrimitives()
 	{
 		Primitives primitives = new Primitives();
 		primitives.setChar( 'c' );
 		primitives.setDouble( 12d );
 		primitives.setFloat( 11f );
 		primitives.setInt( 5 );
 		primitives.setLong( 16l );
 		primitives.setShort( (short)3 );
 		primitives.setString( "astring" );
 		primitives.setBoolean( true );
 		primitives.setByte( (byte)123 );
 
 
 		recorder.record( primitives );
 		assertContains(
 				"	protected Primitives primitives;\n" +
 				"\n" +
 				"	public Primitives buildPrimitives()\n" +
 				"	{\n" +
 				"		primitives = new Primitives();\n" +
 				"		primitives.setBoolean(  Boolean.TRUE );\n" +
 				"		primitives.setByte( new Byte( (byte)123) );\n" +
 				"		primitives.setChar( new Character('c') );\n" +
 				"		primitives.setDouble( new Double( 12.0) );\n" +
 				"		primitives.setFloat( new Float( 11.0) );\n" +
 				"		primitives.setInt( new Integer( 5 ) );\n" +
				"		primitives.setLong( new Long( 16l ) );\n" +
 				"		primitives.setShort( new Short( (short)3) );\n" +
 				"		primitives.setString( \"astring\" );\n" +
 				"		return primitives;\n" +
 				"	}\n");
 	}
 
 	public void testLangObjects()
 	{
 		LangObjects langs = new LangObjects();
 		langs.setCharacter( 'c' );
 		langs.setDouble( 12d );
 		langs.setFloat( 11f );
 		langs.setInteger( 5 );
 		langs.setLong( 16l );
 		langs.setShort( (short)3 );
 		langs.setBoolean( true );
 		langs.setByte( (byte)123 );
 		langs.setClassArgument( Person.class );
 
 		recorder.record( langs );
 		assertContains("	public LangObjects buildLangObjects()\n" +
 				"	{\n" +
 				"		langobjects = new LangObjects();\n" +
 				"		langobjects.setBoolean(  Boolean.TRUE );\n" +
 				"		langobjects.setByte( new Byte( (byte)123) );\n" +
 				"		langobjects.setCharacter( new Character('c') );\n" +
 				"		langobjects.setClassArgument( com.advancedpwr.record.examples.Person.class );\n" +
 				"		langobjects.setDouble( new Double( 12.0) );\n" +
 				"		langobjects.setFloat( new Float( 11.0) );\n" +
 				"		langobjects.setInteger( new Integer( 5 ) );\n" +
				"		langobjects.setLong( new Long( 16l ) );\n" +
 				"		langobjects.setShort( new Short( (short)3) );\n" +
 				"		return langobjects;\n" +
 				"	}");
 
 	}
 
 }
