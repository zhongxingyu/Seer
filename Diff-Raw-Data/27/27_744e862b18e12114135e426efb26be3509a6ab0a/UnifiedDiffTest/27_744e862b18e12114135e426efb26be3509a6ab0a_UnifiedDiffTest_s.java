 package net.sf.yal10n.diff;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link UnifiedDiff}.
  */
 public class UnifiedDiffTest
 {
     /**
      * Tests a diff with only additions.
      */
     @Test
     public void testDiff1()
     {
         String diff = "Index: project-a/project-a-ui/src/main/resources/messages.properties\n"
                 + "===================================================================\n"
                 + "--- project-a/project-a-ui/src/main/resources/messages.properties (revision 15)\n"
                 + "+++ project-a/project-a-ui/src/main/resources/messages.properties (revision 18)\n"
                 + "@@ -1,3 +1,8 @@\n"
                 + "+#\n"
                 + "+# Copyright (C) 2013 MyCompany\n"
                 + "+# All rights reserved.\n"
                 + "+#\n"
                 + "+\n"
                 + " test=test\n"
                 + " another=string\n"
                 + " not=translated\n";
 
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         Assert.assertEquals( "project-a/project-a-ui/src/main/resources/messages.properties (revision 15)",
                 unifiedDiff.getOriginalName() );
         Assert.assertEquals( "project-a/project-a-ui/src/main/resources/messages.properties (revision 18)",
                 unifiedDiff.getNewName() );
         Assert.assertEquals( 1, unifiedDiff.getHunks().size() );
 
         Hunk hunk = unifiedDiff.getHunks().get( 0 );
         Assert.assertEquals( 1, hunk.firstLineNumber );
         Assert.assertEquals( 9, hunk.lastLineNumber );
 
         Assert.assertNull( hunk.origLines.get( 1 ) );
         Assert.assertEquals( "#", hunk.newLines.get( 1 ) );
         Assert.assertEquals( (Character) '+', hunk.indicators.get( 1 ) );
         Assert.assertNull( hunk.origLines.get( 5 ) );
         Assert.assertEquals( "", hunk.newLines.get( 5 ) );
         Assert.assertEquals( (Character) '+', hunk.indicators.get( 5 ) );
         Assert.assertEquals( "test=test", hunk.commonLines.get( 6 ) );
         Assert.assertEquals( (Character) ' ', hunk.indicators.get( 6 ) );
     }
 
     /**
      * Tests a diff where one line has been changed.
      */
     @Test
     public void testDiff2()
     {
         String diff = "Index: project-x/src/main/resources/messages.properties\n"
                 + "===================================================================\n"
                 + "--- project-x/src/main/resources/messages.properties  (revision 18)\n"
                 + "+++ project-x/src/main/resources/messages.properties  (revision 19)\n"
                 + "@@ -3,5 +3,5 @@\n"
                 + " # All rights reserved.\n"
                 + " #\n"
                 + " \n"
                 + "-withHtml=this is a message <strong>which contains</strong> html markup.\n"
                 + "+withHtml=this is a message <strong>which contains</strong> html markup. changed.\n"
                 + " test=test default\n"
                 + "\\ No newline at end of file\n";
 
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         Assert.assertEquals( "project-x/src/main/resources/messages.properties  (revision 18)",
                 unifiedDiff.getOriginalName() );
         Assert.assertEquals( "project-x/src/main/resources/messages.properties  (revision 19)",
                 unifiedDiff.getNewName() );
         Assert.assertEquals( 1, unifiedDiff.getHunks().size() );
 
         Hunk hunk = unifiedDiff.getHunks().get( 0 );
         Assert.assertEquals( 3, hunk.firstLineNumber );
         Assert.assertEquals( 8, hunk.lastLineNumber );
         Assert.assertEquals( "", hunk.commonLines.get( 5 ) );
         Assert.assertEquals( (Character) ' ', hunk.indicators.get( 5 ) );
         Assert.assertEquals( "withHtml=this is a message <strong>which contains</strong> html markup.",
                 hunk.origLines.get( 6 ) );
         Assert.assertEquals( "withHtml=this is a message <strong>which contains</strong> html markup. changed.",
                 hunk.newLines.get( 6 ) );
         Assert.assertEquals( (Character) 'C', hunk.indicators.get( 6 ) );
         Assert.assertEquals( "test=test default", hunk.commonLines.get( 7 ) );
         Assert.assertEquals( (Character) ' ', hunk.indicators.get( 7 ) );
     }
 
     /**
      * Tests a diff with two hunks.
      */
     @Test
     public void testDiff3()
     {
         String diff = "Index: project-x/src/main/resources/messages.properties\n"
                 + "===================================================================\n"
                 + "--- project-x/src/main/resources/messages.properties  (revision 18)\n"
                 + "+++ project-x/src/main/resources/messages.properties  (revision 19)\n"
                 + "@@ -3,5 +3,5 @@\n"
                 + " # All rights reserved.\n"
                 + " #\n"
                 + " \n"
                 + "-withHtml=this is a message <strong>which contains</strong> html markup.\n"
                 + "+withHtml=this is a message <strong>which contains</strong> html markup. changed.\n"
                 + " test=test default\n"
                 + "@@ -10,5 +10,6 @@\n"
                 + " foo=line 10\n"
                 + " foo=line 11\n"
                 + "-foo=line 12\n"
                 + "+foo=line 12 changed\n"
                 + "+foo=line 13 added\n"
                 + " foo=line 14\n"
                 + " foo=line 15\n";
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         Assert.assertEquals( 2, unifiedDiff.getHunks().size() );
 
         Hunk hunk1 = unifiedDiff.getHunks().get( 0 );
         Assert.assertEquals( 3, hunk1.firstLineNumber );
         Assert.assertEquals( 8, hunk1.lastLineNumber );
 
         Hunk hunk2 = unifiedDiff.getHunks().get( 1 );
         Assert.assertEquals( 10, hunk2.firstLineNumber );
         Assert.assertEquals( 16, hunk2.lastLineNumber );
         Assert.assertEquals( "foo=line 12", hunk2.origLines.get( 12 ) );
         Assert.assertEquals( "foo=line 12 changed", hunk2.newLines.get( 12 ) );
         Assert.assertEquals( "foo=line 13 added", hunk2.newLines.get( 13 ) );
         Assert.assertNull( hunk2.origLines.get( 13 ) );
         Assert.assertEquals( "foo=line 14", hunk2.commonLines.get( 14 ) );
     }
 
     /**
      * A single hunk that contains all variants (deletion, additions, changes).
      */
     @Test
     public void testDiff4()
     {
         String diff = "Index: myfile\n"
                 + "==================================\n"
                 + "--- myfile (old)\n"
                 + "+++ myfile (new)\n"
                 + "@@ -1,6 +1,6 @@\n"
                 + " first line - no change\n"
                 + "-second line - is removed\n"
                 + " third line - no change\n"
                 + "-fourth line\n"
                 + "+fourth line - changed\n"
                 + " fifth line - no change\n"
                 + "+sixth line - added\n"
                 + " seventh line - no change\n"
                 + "@@ -10,3 +10,2 @@\n"
                 + " no change\n"
                 + "-removed\n"
                 + " no change\n";
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         Assert.assertEquals( 2, unifiedDiff.getHunks().size() );
 
         String html = unifiedDiff.asHtmlSnippet();
        Assert.assertTrue( html.startsWith( "<table" ) );
         Assert.assertTrue( html.contains( "myfile (old)" ) );
         Assert.assertTrue( html.contains( "myfile (new)" ) );
         Assert.assertTrue( html.contains( "Line 1" ) );
         Assert.assertTrue( html.contains( "Line 10" ) );
 
         String asString = unifiedDiff.toString();
         Assert.assertTrue( asString.contains( "myfile (old)" ) );
         Assert.assertTrue( asString.contains( "myfile (new)" ) );
         Assert.assertTrue( asString.contains( "found 2 hunks" ) );
         Assert.assertTrue( asString.contains( "C4  fourth line || fourth line - changed" ) );
         Assert.assertTrue( asString.contains( "-11  removed" ) );
     }
 
     /**
      * Too short. Not enough lines.
      */
     @Test( expected = IllegalArgumentException.class )
     public void testDiffMalformed1()
     {
         String diff = "This is not a unified diff...";
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         if ( unifiedDiff.getOriginalName() != null )
         {
             Assert.fail( "expected IllegalArgumentException " );
         }
     }
 
     /**
      * Some garbage lines before the first hunk
      */
     @Test
     public void testDiffMalformed2()
     {
         String diff = "Index: foo\n"
                 + "--- original file\n"
                 + "+++ new file\n"
                 + "I'm not starting the diff yet\n"
                 + "here are some arbitrary files that should be ignored\n"
                 + "@@ -1,1 +1,1 @@\n"
                 + " no change\n";
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         Assert.assertEquals( 1, unifiedDiff.getHunks().size() );
     }
 }
