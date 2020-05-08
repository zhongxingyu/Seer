 /*
  * Copyright 2013 Thales Ferreira
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
 package com.thalespf.library.wizardmaker.wizard.internal.shifter;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.thalespf.library.wizardmaker.R;
 import com.thalespf.library.wizardmaker.wizard.IWizardPage;
 import com.thalespf.library.wizardmaker.wizard.internal.WizardContainer.WizardContainerController;
 
 
 /**
  * @author Thales Ferreira / l.thales.x@gmail.com
  *
  */
 public class ShifterTop extends AbstractShifter {
 
 	/**
 	 * @param controller
 	 */
 	public ShifterTop(WizardContainerController controller) {
 		super(controller);
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.shifter.AbstractShifter#onCreateShifterView()
 	 */
 	@Override
 	protected View onCreateShifterView() {
 		View view = LayoutInflater.from(wizardActivity).inflate(R.layout.wizard_maker_shifter_view, null);
		view.findViewById(R.id.shifterLeftButton).setEnabled(false);
		view.findViewById(R.id.shifterRigthButton).setEnabled(false);
		TextView t = (TextView) view.findViewById(R.id.shifterPagesText);
		t.setText("(0/0)");
 		return view;
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.shifter.AbstractShifter#updateShifter(int, android.support.v4.view.ViewPager, com.thalespf.library.wizardmaker.wizard.IWizard)
 	 */
 	@Override
 	public void updateShifter(IWizardPage page, int currentItem, int lastItem) {
 		super.updateShifter(page, currentItem, lastItem);
 		
 		//change the next button to finish button
 		if(currentItem == lastItem) {		
 			ImageButton rButton = (ImageButton) shifterView.findViewById(R.id.shifterRigthButton);
 			rButton.setVisibility(View.GONE);
 			
 			ImageButton finishButton = (ImageButton) shifterView.findViewById(R.id.shifterFinishButton);
 			finishButton.setVisibility(View.VISIBLE);
 			finishButton.setEnabled(true);
 		} else {
 			ImageButton rButton = (ImageButton) shifterView.findViewById(R.id.shifterRigthButton);
 			rButton.setVisibility(View.VISIBLE);
 			rButton.setEnabled(true);
 			
 			ImageButton finishButton = (ImageButton) shifterView.findViewById(R.id.shifterFinishButton);
 			finishButton.setVisibility(View.GONE);
 		}
 		
 		shifterView.requestLayout();
 		
 		TextView pt = (TextView) shifterView.findViewById(R.id.shifterPagesText);
 		int actual = currentItem+1;
 		pt.setText("("+actual+"/"+(lastItem + 1)+")");
 	}
 
 }

