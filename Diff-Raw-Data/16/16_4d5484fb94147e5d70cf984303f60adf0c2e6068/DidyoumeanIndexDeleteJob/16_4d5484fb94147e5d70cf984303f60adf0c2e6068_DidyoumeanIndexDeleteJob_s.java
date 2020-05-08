 package com.gentics.cr.lucene.didyoumean;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.search.spell.CustomSpellChecker;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
 import com.gentics.cr.util.indexing.IndexLocation;
 
 /**
  * This job is used to clear a lucene didyoumean index.
  * 
  * @author Sebastian Vogel <s.vogel@gentics.com>
  * 
  */
 public class DidyoumeanIndexDeleteJob extends AbstractUpdateCheckerJob {
 
 	private DidyoumeanIndexExtension didyoumean;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param config configuration
 	 * @param indexLoc indexLocation
 	 * @param didyoumean the {@link com.gentics.cr.lucene.didyoumean.DidyoumeanIndexExtension} to clear
 	 */
 	public DidyoumeanIndexDeleteJob(final CRConfig config, final IndexLocation indexLoc,
 		DidyoumeanIndexExtension didyoumean) {
 		super(config, indexLoc, null);
 		log = Logger.getLogger(DidyoumeanIndexDeleteJob.class);
 
 		this.identifyer = identifyer.concat(":clear");
 		this.didyoumean = didyoumean;
 	}
 
 	/**
 	 * starts the job - is called by the IndexJobQueue.
 	 */
 	@Override
 	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {
 
 		log.debug("Starting to clear index.");
 		CustomSpellChecker spellchecker = didyoumean.getSpellchecker();
 		try {
 			spellchecker.clearIndex();
 			didyoumean.getDidyoumeanLocation().resetIndexJobCreationTimes();
 		} catch (IOException e) {
 			log.error("Could not clear index", e);
 		}
 		log.debug("Finished clearing index.");
 
 	}
 
 }
