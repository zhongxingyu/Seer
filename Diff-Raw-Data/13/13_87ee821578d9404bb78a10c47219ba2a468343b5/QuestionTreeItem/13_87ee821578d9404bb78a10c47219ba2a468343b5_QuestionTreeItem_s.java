 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.client.ui;
 
 import com.google.gwt.user.client.ui.TreeItem;
 
 /**
  * This class defines a single item in a GWT tree widget.
  * 
  * <p>Used in both the {@link QuestionPanel} and in
  * the {@link PerspectiveQuestionsPanel}.</p>
  */
 class QuestionTreeItem extends TreeItem {
 
 	String questionID;
 
 	QuestionTreeItem(String text, String questionID) {
		super(text);
 		this.questionID = questionID;
 	}
 
 	/**
 	 * Get the question id.
 	 * 
 	 * @return the question id
 	 */
 	public String getQuestionID() {
 		return questionID;
 	}
 }
