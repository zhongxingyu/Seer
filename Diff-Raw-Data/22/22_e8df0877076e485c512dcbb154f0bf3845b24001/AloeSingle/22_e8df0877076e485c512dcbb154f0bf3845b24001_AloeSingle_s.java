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
 
 import etc.aloe.controllers.LabelingController;
 import etc.aloe.data.FeatureSpecification;
 import etc.aloe.data.Message;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Model;
 import etc.aloe.data.Segment;
 import etc.aloe.data.SegmentSet;
 import etc.aloe.options.InteractiveOptions;
 import etc.aloe.options.ModeOptions;
 import etc.aloe.options.SingleOptions;
 import java.util.Date;
 
 /**
  * Controller for single-message label mode.
  *
  * @author Michael Brooks <mjbrooks@uw.edu>
  */
 public class AloeSingle extends Aloe {
 
    InteractiveOptions options;

     @Override
     public void run(ModeOptions modeOptions) {
         System.out.println("== Preparation ==");
 
         if (modeOptions instanceof SingleOptions) {
             SingleOptions options = (SingleOptions) modeOptions;
 
             String messageStr = options.messageText;
             String participant = "stdin";
             Date time = new Date();
 
             MessageSet messages = new MessageSet();
             Message message = new Message(0, time, participant, messageStr);
             messages.add(message);
 
             //Make a segment for the message
             Segment segment = new Segment();
             segment.add(message);
             SegmentSet segmentSet = new SegmentSet();
             segmentSet.add(segment);
 
             LabelingController labelingController = new LabelingController();
             labelingController.setFeatureExtractionImpl(factory.constructFeatureExtraction());
             labelingController.setEvaluationImpl(factory.constructEvaluation());
             labelingController.setMappingImpl(factory.constructLabelMapping());
 
             FeatureSpecification spec = this.loadFeatureSpecification(options.inputFeatureSpecFile);
             Model model = this.loadModel(options.inputModelFile);
 
             labelingController.setModel(model);
             labelingController.setFeatureSpecification(spec);
             labelingController.setSegmentSet(segmentSet);
             labelingController.run();
 
             System.out.println("" + message.getPredictedLabel());
         } else {
             throw new IllegalArgumentException("Options must be for Single");
         }
     }
 }
