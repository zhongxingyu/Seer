 package com.github.hermod.ser.descriptor;
 
 import static org.assertj.core.api.Assertions.assertThat;
 
 import org.junit.Test;
 
 /**
  * <p>ReverseEnumMapTest. </p>
  *
  * @author anavarro - Apr 13, 2013
  *
  */
 public class ReverseEnumMapTest {
 
 
     /**
      * testGet.
      *
      */
     @Test
     public void testGet() {
        final ReverseEnumMap<FootballPlayer> map = new ReverseEnumMap<FootballPlayer>(FootballPlayer.class);
         assertThat(map.get(1)).isEqualTo(FootballPlayer.GOALKEEPER);
     }
 
 }
