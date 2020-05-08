 package org.anc.lapps.masc;
 
import org.anc.index.api.Index;
 import org.anc.lapps.masc.index.MascTextIndex;
 import org.lappsgrid.api.Data;
 import org.lappsgrid.api.DataSource;
 import org.lappsgrid.core.DataFactory;
 import org.lappsgrid.discriminator.DiscriminatorRegistry;
 import org.lappsgrid.discriminator.Types;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 /**
  * A {@link org.lappsgrid.api.DataSource DataSource} for accessing the text
  * files from the MASC.
  *
  * @author Keith Suderman
  */
 public class MascTextSource extends AbstractDataSource
 {
    public MascTextSource() throws IOException
    {
       super(new MascTextIndex());
       System.out.println("Created a MASC text datasource.");
    }
 
    @Override
    /**
     * The text data source either returns an error or the actual text.
     */
    protected Data get(String key)
    {
       System.out.println("Getting text for " + key);
       Data result = super.get(key);
       if (result.getDiscriminator() != Types.ERROR)
       {
          result.setDiscriminator(Types.TEXT);
       }
       return result;
    }
 }
