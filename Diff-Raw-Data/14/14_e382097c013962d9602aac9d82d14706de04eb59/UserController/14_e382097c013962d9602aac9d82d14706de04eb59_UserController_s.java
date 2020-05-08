 /*
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
 
 
 package ca.cmput301f13t03.adventure_datetime.controller;
 
 import ca.cmput301f13t03.adventure_datetime.model.*;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IStoryModelDirector;
 
 public class UserController
 {
 	private IStoryModelDirector m_storyDirector = null;
 	
    public UserController(IStoryModelDirector director)
     {
     	m_storyDirector = director;
     }
 
     public void StartStory(long storyId)
     {
     	m_storyDirector.SelectStory(storyId);
     	/* TODO::JT also select head fragment and create save*/
     }
 
     public void ResumeStory(long bookmarkId)
     {
     	/* TODO::JT find the bookmark, select its story and its fragment */
     }
 
     public void SetBookmark()
     {
     	/* TODO::JT Create a bookmark for the current story and fragment */
     }
 
     public void AddComment(Comment comment)
     {
     	/* TODO::JT */
     }
 
     public void MakeChoice(long choiceId)
     {
     	/* TODO::JT map choice to fragment then set that fragment */
     }
 }
