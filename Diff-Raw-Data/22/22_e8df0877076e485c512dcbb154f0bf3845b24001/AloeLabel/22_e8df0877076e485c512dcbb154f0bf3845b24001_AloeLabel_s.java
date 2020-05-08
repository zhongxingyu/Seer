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
 import etc.aloe.data.EvaluationReport;
 import etc.aloe.data.FeatureSpecification;
 import etc.aloe.data.MessageSet;
 import etc.aloe.data.Model;
 import etc.aloe.data.SegmentSet;
 import etc.aloe.options.LabelOptions;
 import etc.aloe.options.ModeOptions;
 import etc.aloe.processes.Segmentation;
 
 /**
  * Class that takes input data (may be labeled, unlabeled, or mixed) and applies
  * an existing model (and feature set) to it in order to generate labels.
  *
  * If there are any labeled examples in the input data, an evaluation is
  * generated comparing the predicted labels to the true labels.
  *
  * @author Michael Brooks <mjbrooks@uw.edu
  */
 public class AloeLabel extends Aloe {
 
    LabelOptions options;

     @Override
     public void run(ModeOptions modeOptions) {
         System.out.println("== Preparation ==");
 
         if (modeOptions instanceof LabelOptions) {
             //Set up the segmentation
             Segmentation segmentation = factory.constructSegmentation();
 
             //Create a labeling controller
             LabelingController labelingController = new LabelingController();
 
             //Provide implementations of the needed processes
             labelingController.setFeatureExtractionImpl(factory.constructFeatureExtraction());
             labelingController.setEvaluationImpl(factory.constructEvaluation());
             labelingController.setMappingImpl(factory.constructLabelMapping());
 
             //Process the input messages
             MessageSet messages = this.loadMessages(options.inputCSVFile);
             FeatureSpecification spec = this.loadFeatureSpecification(options.inputFeatureSpecFile);
             Model model = this.loadModel(options.inputModelFile);
 
             SegmentSet segments = segmentation.segment(messages);
 
             //Run the labeling process
             labelingController.setModel(model);
             labelingController.setSegmentSet(segments);
             labelingController.setFeatureSpecification(spec);
             labelingController.run();
 
             //Get the outputs
             EvaluationReport evalReport = labelingController.getEvaluationReport();
 
             System.out.println("== Saving Output ==");
 
             saveEvaluationReport(evalReport, options.outputEvaluationReportFile);
             saveMessages(messages, options.outputCSVFile);
 
             System.out.println("Testing Report:");
             System.out.println(evalReport);
             System.out.println("---------");
         } else {
             throw new IllegalArgumentException("Options must be for Labeling");
         }
     }
 }
