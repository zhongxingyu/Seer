 package com.ngdb;
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class MarkTest {
 
     @Test
     public void should_transform_percent() {
         assertThat(new Mark("0").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("50%").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("51%").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("52%").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("53%").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("54%").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("55%").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("56%").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("57%").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("58%").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("59%").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("60%").getStarsAsDouble()).isEqualTo(1);
         assertThat(new Mark("70%").getStarsAsDouble()).isEqualTo(2);
         assertThat(new Mark("80%").getStarsAsDouble()).isEqualTo(3);
         assertThat(new Mark("90%").getStarsAsDouble()).isEqualTo(4);
         assertThat(new Mark("100%").getStarsAsDouble()).isEqualTo(5);
     }
 
     @Test
     public void should_transform_to_string() {
         assertThat(new Mark("85%").toString()).isEqualTo("35");
         assertThat(new Mark("88%").toString()).isEqualTo("35");
         assertThat(new Mark("0").toString()).isEqualTo("00");
         assertThat(new Mark("50%").toString()).isEqualTo("00");
         assertThat(new Mark("51%").toString()).isEqualTo("00");
         assertThat(new Mark("52%").toString()).isEqualTo("00");
         assertThat(new Mark("53%").toString()).isEqualTo("00");
         assertThat(new Mark("54%").toString()).isEqualTo("00");
         assertThat(new Mark("55%").toString()).isEqualTo("05");
         assertThat(new Mark("56%").toString()).isEqualTo("05");
         assertThat(new Mark("57%").toString()).isEqualTo("05");
         assertThat(new Mark("58%").toString()).isEqualTo("05");
         assertThat(new Mark("59%").toString()).isEqualTo("05");
         assertThat(new Mark("60%").toString()).isEqualTo("10");
         assertThat(new Mark("70%").toString()).isEqualTo("20");
         assertThat(new Mark("80%").toString()).isEqualTo("30");
         assertThat(new Mark("90%").toString()).isEqualTo("40");
         assertThat(new Mark("100%").toString()).isEqualTo("50");
     }
 
     @Test
    public void should_transform_mark_on_5_in_percent() {
        assertThat(new Mark("2/5").getAsPercent()).isEqualTo(70);
    }

    @Test
     public void should_transform_to_percent() {
         assertThat(new Mark("0").getAsPercent()).isEqualTo(0);
         assertThat(new Mark("50%").getAsPercent()).isEqualTo(50);
         assertThat(new Mark("51%").getAsPercent()).isEqualTo(51);
         assertThat(new Mark("52%").getAsPercent()).isEqualTo(52);
         assertThat(new Mark("53%").getAsPercent()).isEqualTo(53);
         assertThat(new Mark("54%").getAsPercent()).isEqualTo(54);
         assertThat(new Mark("55%").getAsPercent()).isEqualTo(55);
         assertThat(new Mark("56%").getAsPercent()).isEqualTo(56);
         assertThat(new Mark("57%").getAsPercent()).isEqualTo(57);
         assertThat(new Mark("58%").getAsPercent()).isEqualTo(58);
         assertThat(new Mark("59%").getAsPercent()).isEqualTo(59);
         assertThat(new Mark("60%").getAsPercent()).isEqualTo(60);
         assertThat(new Mark("70%").getAsPercent()).isEqualTo(70);
         assertThat(new Mark("80%").getAsPercent()).isEqualTo(80);
         assertThat(new Mark("90%").getAsPercent()).isEqualTo(90);
         assertThat(new Mark("100%").getAsPercent()).isEqualTo(100);
     }
 
     @Test
     public void should_transform_stars_on_5() {
         assertThat(new Mark("0/5").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("0,1/5").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("0,2/5").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("0,3/5").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("0,4/5").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("0,5/5").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("0,6/5").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("0,7/5").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("0,8/5").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("0,9/5").getStarsAsDouble()).isEqualTo(0.5);
         assertThat(new Mark("1/5").getStarsAsDouble()).isEqualTo(1);
         assertThat(new Mark("2/5").getStarsAsDouble()).isEqualTo(2);
         assertThat(new Mark("3/5").getStarsAsDouble()).isEqualTo(3);
         assertThat(new Mark("4/5").getStarsAsDouble()).isEqualTo(4);
         assertThat(new Mark("5/5").getStarsAsDouble()).isEqualTo(5);
     }
 
     @Test
     public void should_transform_stars_on_6() {
         assertThat(new Mark("0/6").getStarsAsDouble()).isEqualTo(0);
         assertThat(new Mark("3/6").getStarsAsDouble()).isEqualTo(2.5);
         assertThat(new Mark("6/6").getStarsAsDouble()).isEqualTo(5);
     }
 
 }
