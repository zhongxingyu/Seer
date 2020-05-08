 package ca.cmput301f13t03.adventure_datetime.model;/*
  * Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * Evan DeGraff
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 import android.test.AndroidTestCase;
 import android.test.RenamingDelegatingContext;
 import junit.framework.Assert;
 
 import java.util.UUID;
 
 /**
  * @author Andrew Fontaine
  * @version 1.0
  * @since 31/10/13
  */
 public class StoryDBTest extends AndroidTestCase {
 
 	private StoryDB database;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();    //TODO Implement
 		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
 		database = new StoryDB(context);
 	}
 
 	public void testSetStoryFragment() throws Exception {
 
 		String uuid = UUID.randomUUID().toString();
 		Choice choice = new Choice("test", 5);
 		StoryFragment frag = new StoryFragment(uuid, "testing", choice);
 
		String fragUuid = frag.getFragmentID();

 		Assert.assertTrue("Error inserting fragment", database.setStoryFragment(frag));
 		StoryFragment frag2 = database.getStoryFragment(frag.getFragmentID());
 
 		Assert.assertEquals("Not equivalent fragment ids", frag.getFragmentID(), frag2.getFragmentID());
 		Assert.assertEquals("Not equivalent story ids", frag.getStoryID(), frag2.getStoryID());
 
		Assert.assertEquals("Not equivalent UUIDs", fragUuid, frag.getFragmentID());

 	}
 
 
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();    //TODO Implement
 	}
 
 
 }
