 package de.unifr.acp.test;
 
 public class TestArrayClass {
     TestEmptyClass[][][] oa1 = new TestEmptyClass[][][] { { { null } }};
     Object[][] oa2 = new TestArrayClass[][] { { null }, { null } };;
    int[] ia = { 1, 2, 3 };
 
    @de.unifr.acp.runtime.annot.Grant("this.(oa1|ia), this.oa2.(ia|oa1)")
     public void m() {
         oa1 = new TestEmptyClass[1][1][1];
         oa1[0][0][0] = null;
        ((TestArrayClass) oa2[0][0]).ia = null;
        ((TestArrayClass) oa2[0][0]).oa1[0][0] = null;
         ia = new int[] { 1, 2, 3, 4 };
     }
 
     public void setup() {
         oa2 = new TestArrayClass[][] { { new TestArrayClass() },
                 { new TestArrayClass() } };
         ;
     }
 
     public static void main(String[] args) {
         TestArrayClass arrayObject = new TestArrayClass();
         arrayObject.setup();
         arrayObject.m();
     }
 
 }
