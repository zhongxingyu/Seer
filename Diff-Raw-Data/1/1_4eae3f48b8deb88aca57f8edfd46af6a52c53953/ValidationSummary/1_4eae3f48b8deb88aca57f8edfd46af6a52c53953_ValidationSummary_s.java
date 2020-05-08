 /**
  * Android validators.
  * Copyright (c) 2011 Ruswizards LLC <dsitnikov@ruswizards.com>
  * http://ruswizards.com / https://github.com/ruswizards/AndroidValidators
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.ruswizards.android.validators;
 
 import java.util.HashMap;
 
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * Check state of multiple validators to give one response. 
  * @author Dmitry Sitnikov
  */
 public class ValidationSummary implements ValidationChangesListener {
 
 	/** Map of all validators and their state */
 	HashMap<BaseValidator, Boolean> validatorsState_ = new HashMap<BaseValidator, Boolean>();
 	
 	//Listened validators state - true when all validator has no errors.
 	boolean isCurrentStateCorrect_ = false;
 	
 	String errorMessage_;
 	TextView errorView_;
 	
 	/**
 	 * Create validator summary and set error message and view to display it.
 	 * Set errorView visibility to GONE at call.
 	 * @param errorMessage error message.
 	 * @param errorView view to display error.
 	 */
 	public ValidationSummary(String errorMessage, TextView errorView) {
 		errorMessage_ = errorMessage;
 		errorView_ = errorView;
 		errorView_.setVisibility(View.GONE);
 	}
 	
 	@Override
 	public void onValidationStateUpdated(BaseValidator sender, boolean isValid) {
 		validatorsState_.put(sender, isValid);
 		for (BaseValidator validator: validatorsState_.keySet()) {
 			if ( !validatorsState_.get(validator)) { //One mismatch - set false to state.
 				isCurrentStateCorrect_ = false;
 				errorView_.setVisibility(View.VISIBLE);
 				return;
 			}
 		}
 		//No bad validators in list.
 		errorView_.setVisibility(View.GONE);
 		isCurrentStateCorrect_ = true;
 	}
 	
 	/**
 	 * Add validator to control their state.
 	 * Do not modify current state of ValidationSummary.
 	 */
 	public void addValidator(BaseValidator validator) {
 		if (!validatorsState_.containsKey(validator)) {
 			validatorsState_.put(validator, true);
 			validator.setChangesListener(this);
 		}
 	}
 	
 	/**
 	 * Is all validators do not contains errors.
 	 */
 	public boolean isCorrect() {
 		return isCurrentStateCorrect_;
 	}
 	
 	/**
 	 * Force check for all nested validators. 
 	 */
 	public void performCheck() {
 		for (BaseValidator validator: validatorsState_.keySet()) {
 			validator.displayError(validator.checkConditions());
 		}
 	}
 }
