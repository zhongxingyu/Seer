 /*
  * Copyright 2013- Yan Bonnel
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.ybonnel.prenoms;
 
 import fr.ybonnel.csvengine.annotation.CsvColumn;
 import fr.ybonnel.csvengine.annotation.CsvFile;
 
 import java.util.List;
 
 @CsvFile(separator = ";")
 public class PrenomTxt {
     //01_prenom;02_genre;03_langage;04_fréquence
     @CsvColumn("01_prenom")
     public String prenom;
     @CsvColumn("02_genre")
     public String genre;
    @CsvColumn(value = "03_language", adapter = AdapterLangage.class)
     public List<String> langages;
 }
