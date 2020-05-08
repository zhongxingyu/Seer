 /*
  * This file is part of ALOE.
  *
  * ALOE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * ALOE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ALOE.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2012 SCCL, University of Washington (http://depts.washington.edu/sccl)
  */
 package etc.aloe;
 
 import etc.aloe.controllers.InteractiveController;
 import etc.aloe.data.FeatureSpecification;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Model;
 import etc.aloe.options.InteractiveOptions;
 import etc.aloe.options.ModeOptions;
 
 /**
  * Controller for interactive mode.
  *
  * @author Michael Brooks <mjbrooks@uw.edu>
  */
 public class AloeInteractive extends Aloe {
 
     @Override
     public void run(ModeOptions modeOptions) {
         System.out.println("== Preparation ==");
 
         if (modeOptions instanceof InteractiveOptions) {
            InteractiveOptions options = (InteractiveOptions) modeOptions;

             InteractiveController interactiveController = new InteractiveController();
 
             //Provide implementations for the controller
             interactiveController.setFeatureExtractionImpl(factory.constructFeatureExtraction());
             interactiveController.setMappingImpl(factory.constructLabelMapping());
 
             FeatureSpecification spec = this.loadFeatureSpecification(options.inputFeatureSpecFile);
             Model model = this.loadModel(options.inputModelFile);
 
             interactiveController.setModel(model);
             interactiveController.setFeatureSpecification(spec);
             interactiveController.run();
 
             System.out.println();
             System.out.println("== Saving Output ==");
 
             MessageSet messages = interactiveController.getMessageSet();
             messages.setDateFormat(factory.constructDateFormat());
             saveMessages(messages, options.outputCSVFile);
         } else {
             throw new IllegalArgumentException("Options must be for Interactive");
         }
     }
 }
