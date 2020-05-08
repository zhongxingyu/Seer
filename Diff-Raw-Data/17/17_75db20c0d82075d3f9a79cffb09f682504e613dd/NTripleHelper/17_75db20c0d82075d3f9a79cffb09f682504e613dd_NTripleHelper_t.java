 package net.croz.scardf.query;
 
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
 
 class NTripleHelper extends NTripleWriter {
    public static String ntRendering(Resource r) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        NTripleWriter.writeResource(r, pw);
        return sw.toString();
    }
 }
