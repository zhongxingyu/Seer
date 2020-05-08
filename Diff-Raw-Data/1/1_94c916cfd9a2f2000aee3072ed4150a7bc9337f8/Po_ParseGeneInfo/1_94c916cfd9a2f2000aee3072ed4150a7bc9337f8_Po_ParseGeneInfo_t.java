 /*
  * Copyright (c) 2012, Pierre-Yves Chibon
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the Wageningen University nor the names of its
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ''AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
  * THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package nl.wur.plantbreeding.gff2RDF.Potato;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import nl.wur.plantbreeding.gff2RDF.ObjectToModel;
 import nl.wur.plantbreeding.gff2RDF.object.Gene;
 
 /**
  * This class parses the GFF file containing the genes information from PGSC,
  * release 3.4.
  *
  * As format may change, the parser may need changes for futur release.
  * @author Pierre-Yves Chibon -- py@chibon.fr
  */
 public class Po_ParseGeneInfo {
 
     /** Logger used for outputing log information. */
     private static final Logger LOG = Logger.getLogger(
             Po_ParseGeneInfo.class.getName());
 
     /**
      * This method parse the GFF file from TAIR and add the information
      * retrieved about the genes into the model given as parameter.
      * The gene information retrieved by this method are:
      *   - Gene ID
      *   - Gene position on the genome (physical position):
      *       - Chromosome
      *       - Start position
      *       - Stop position
      *
      * @param inputfilename the path to the input file containing the gene
      * information
      * @param model a Jena model in which the gene information will be stored
      * @return a Jena model containing with its previous information the gene
      * information retrieved by this method.
      * @throws IOException When something goes wrong with a file.
      */
     public final Model getModelFromGff(final String inputfilename,
             Model model) throws IOException {
 
         System.out.println("Parsing: " + inputfilename
                 + " and adding information to a model of size " + model.size());
 
         ObjectToModel obj2m = new ObjectToModel();
 
         int cnt = 0;
         int genecnt = 0;
         String strline = "";
         final FileInputStream fstream = new FileInputStream(inputfilename);
         // Get the object of DataInputStream
         final DataInputStream in = new DataInputStream(fstream);
         final BufferedReader br =
                 new BufferedReader(new InputStreamReader(in));
         Gene gene = null;
         //Read File Line By Line
         while ((strline = br.readLine()) != null) {
             strline = strline.trim();
             String[] content = strline.split("\t");
             if (content.length > 3 && content[2].equalsIgnoreCase("gene")) {
                 gene = new Gene();
                 gene.setChromosome(content[0].trim());
                 gene.addPosition(content[3], content[4]);
                 String locus = content[content.length - 1].split(
                         "ID=")[1].split(";")[0];
                 gene.setLocus(locus);
                gene.setType("mRNA:gene");
 
                 // Add gene to model
                 model = obj2m.addToModel(gene, model);
                 genecnt = genecnt + 1;
             }
             cnt = cnt + 1;
         }
         in.close();
 
         if (gene != null) {
             // add gene to model here
             model = obj2m.addToModel(gene, model);
         }
 
         LOG.log(Level.FINE, cnt + " lines read");
         LOG.log(Level.FINE, genecnt + " genes found");
         LOG.log(Level.FINE, "Model has size: " + model.size());
 
         return model;
     }
 }
