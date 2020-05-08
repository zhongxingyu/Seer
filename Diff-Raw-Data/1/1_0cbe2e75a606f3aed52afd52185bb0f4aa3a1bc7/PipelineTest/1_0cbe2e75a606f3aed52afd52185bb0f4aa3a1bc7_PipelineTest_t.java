 package com.thoughtworks.buildnotifier.domain;
 
 import com.thoughtworks.buildnotifier.mothers.JobMother;
 import com.thoughtworks.buildnotifier.mothers.StageMother;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 public class PipelineTest {
     @Test
     public void testEquals() {
         Pipeline pipeline = new Pipeline("name");
         assertTrue(pipeline.equals(new Pipeline("name")));
         assertTrue(new Pipeline("name").equals(pipeline));
 
         assertFalse(pipeline.equals(new Pipeline("other-name")));
         assertFalse(pipeline.equals(new Pipeline("")));
 
         assertFalse(pipeline.equals(null));
         assertFalse(pipeline.equals("name"));
     }
 
     @Test
     public void addJobToStage() {
         Pipeline pipeline = new Pipeline("pipe");
         pipeline.addStage(StageMother.create("stage"));
 
         Job job = JobMother.create();
         pipeline.addJobToStage("stage", job);
         Stage stage = pipeline.stageAt(0);
 
         assertEquals(stage.jobAt(0), job);
     }
 }
