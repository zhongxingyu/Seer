 /**
  * Copyright (c) 2012 Kiselyov A.V., All Rights Reserved
  *
  * For information about the licensing and copyright of this document please
  * contact Alexey Kiselyov. at voilesik@gmail.com
  *
  * @Project: Cortege
  */
 package com.rumba.cortege.examples;
 
 import com.rumba.cortege.Cortege;
 import com.rumba.cortege.CortegeChain;
 
 /**
  * User: kiselyov
  * Date: 21.05.12
  */
 public class ExampleCortege {
     public static void main(String[] args) {
         Cortege<Long, Cortege<String, Cortege.End>> cortegeLS = CortegeChain.create(2);
         // заполнение элементов значениями
         // 1-й вариант (заполняем первый элемент в кортеже, с контролем типа)
         cortegeLS.setValue(4L);
         cortegeLS.nextElement().setValue("str");
         // 2-й вариант (заполняем подряд цепью, с контролем типа)
         cortegeLS.setValue(4L).setValue("str");
        // 3-й вариант (заполняем массивом, с без контроля типа)
         cortegeLS.setValues(4L, "str");
 
         // 1-й вариант (чтение первого элемента в кортеже, с контролем типа)
         Long valueA = cortegeLS.getValue();
         // 2-й вариант (чтение выбранного элемента в кортеже, с контролем типа)
         String valueB = cortegeLS.nextElement().getValue();
         // 3-й вариант (чтение выбранного элемента в кортеже, без контроля типа)
         Long valueC = cortegeLS.getValue(1);
         String valueD = cortegeLS.getValue(2);
     }
 }
