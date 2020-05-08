 package com.abudko.reseller.huuto.query.service.list;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class SizeParserTest {
 
     @Test
     public void testGetSizeCM() {
         assertEquals("60", SizeParser.getSize("timberland toppapuku 60cm 3m"));
     }
 
     @Test
     public void testGetSizeCMWithSpace() {
         assertEquals("86", SizeParser.getSize("86 cm uusi reimatec biret talvihaalari hki ovh  149"));
     }
 
     @Test
     public void testGetSizeCMWith2Spaces() {
         assertEquals("86", SizeParser.getSize("86  cm uusi reimatec biret talvihaalari hki ovh  149"));
     }
 
     @Test
     public void testGetSizeKoko() {
         assertEquals("21", SizeParser.getSize("viking goretex  talvikengat koko21 uudet unisex"));
     }
 
     @Test
     public void testGetSizeKokoWithSpace() {
         assertEquals("21", SizeParser.getSize("viking goretex  talvikengat koko 21 uudet unisex"));
     }
 
     @Test
     public void testGetSizeKokoWith2Spaces() {
         assertEquals("21", SizeParser.getSize("viking goretex  talvikengat koko  21 uudet unisex"));
     }
 
     @Test
     public void testGetSizeKokoWithCM() {
         assertEquals("21", SizeParser.getSize("viking goretex  talvikengat koko 21 uudet unisex 98cm"));
     }
     
     @Test
     public void testGetSizeKokoWithOvh() {
         assertEquals("74", SizeParser.getSize("reima tec  toppahaalari  koko 74   villa pipo  ovh138e"));
     }
 
     @Test
     public void testGetSize1Number() {
         assertEquals("21", SizeParser.getSize("viking goretex  talvikengat 21"));
     }
 
     @Test
     public void testGetSize2Number() {
         assertEquals("21", SizeParser.getSize("viking goretex  talvikengat 273 21"));
     }
 
     @Test
     public void testGetSize2NumberOvh() {
         assertEquals("273", SizeParser.getSize("viking goretex  talvikengat 273 ovh21"));
     }
     
     @Test
     public void testGetSizeOVH() {
         assertEquals("92", SizeParser.getSize("UUSI TICKET TO HEAVEN TALVIHAALARI NORTH 92(86)cm OVH 169e"));
     }
 
     @Test
     public void testGetSize2NumberOvhWithSpace() {
         assertEquals("273", SizeParser.getSize("viking goretex  talvikengat 273 ovh 21"));
     }
 
     @Test
     public void testGetSize2NumberOvhWith2Spaces() {
         assertEquals("273", SizeParser.getSize("viking goretex  talvikengat 273 ovh  21"));
     }
     
     @Test
     public void testGetSize2NumberOvhWithDot() {
         assertEquals("273", SizeParser.getSize("viking goretex  talvikengat 273 ovh.21"));
     }
     
     @Test
     public void testGetSize2NumberOvhWithDotAndSpace() {
         assertEquals("273", SizeParser.getSize("viking goretex  talvikengat 273 ovh. 21"));
     }
 
     @Test
     public void testGetSize2NumberOvhWithDotAnd2Spaces() {
         assertEquals("273", SizeParser.getSize("viking goretex  talvikengat 273 ovh.  21"));
     }
     
     @Test
     public void testGetSizehDotAnd2Spaces() {
         assertEquals("92", SizeParser.getSize(" reimatec toppahousut koko 92 cm  ovh 59 "));
     }
     
     @Test
     public void testGetSizeNRO() {
         assertEquals("98", SizeParser.getSize("reimatec-valikausihousut-98-unisex-lime-nro1"));
     }
     
     @Test
     public void testGetSizeSlash() {
         assertEquals("122", SizeParser.getSize("Ticket to Heaven T2H -tähtihaalari välikausi 122/7 UUSI"));
     }
     
     @Test
     public void testGetSizeEuroa() {
         assertEquals("20", SizeParser.getSize("Viking Gore-Tex talvikengät 20 Uudet! (79 euroa)"));
     }
     
     @Test
     public void testGetSizeComma() {
         assertEquals("27", SizeParser.getSize("Reima Rusko liila - harmaat talvikengät 27 / 17,7 cm UUDET"));
     }
    
    @Test
    public void testGetSizeSizeRange() {
        assertEquals("116", SizeParser.getSize("Color Kids talvihaalari 5v 110-116 ovh 95€"));
    }
 }
