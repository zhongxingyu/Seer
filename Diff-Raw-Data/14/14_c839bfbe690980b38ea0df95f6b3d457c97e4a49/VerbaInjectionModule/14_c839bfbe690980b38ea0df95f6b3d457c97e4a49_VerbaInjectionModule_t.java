 package org.verba.mobile.utils;
 
 import static org.verba.mobile.Verba.getVerbaDirectory;
 
import org.verba.DictionaryEntryRepository;
import org.verba.DictionaryRepository;
 import org.verba.boundary.NewDictionariesScanner;
 import org.verba.boundary.PhraseLookup;
 import org.verba.boundary.SuggestionAdviser;
 import org.verba.interactors.NewStardictDictionariesScanner;
 import org.verba.interactors.SimpleSuggestionAdviser;
 import org.verba.interactors.StardictPhraseLookup;
 import org.verba.mobile.Verba;
 import org.verba.mobile.card.CardDao;
 import org.verba.mobile.card.CardSetDao;
 import org.verba.mobile.repository.SqliteDictionaryEntryRepository;
 import org.verba.mobile.repository.SqliteDictionaryRepository;
 import org.verba.stardict.definitions.StardictDictionaryDefinitionsGateway;
 import org.verba.stardict.index.DictionaryIndexGateway;
 import org.verba.stardict.index.StardictDictionaryIndexGateway;
 import org.verba.stardict.metadata.DictionaryMetadataGateway;
 import org.verba.stardict.metadata.StardictDictionaryMetadataGateway;
 import org.verba.util.DictionaryFileFinder;
 
 import android.content.Context;
 
 import com.google.inject.AbstractModule;
 
 public class VerbaInjectionModule extends AbstractModule {
 	private Verba verbaApplication;
 
 	public VerbaInjectionModule(Context application) {
 		this.verbaApplication = (Verba) application;
 	}
 
 	@Override
 	protected void configure() {
 		DictionaryFileFinder dictionaryFileFinder = new DictionaryFileFinder(getVerbaDirectory());
 
 		SqliteDictionaryRepository dictionaryRepository = verbaApplication.getDictionaryRepository();
 		SqliteDictionaryEntryRepository entryRepository = verbaApplication.getDictionaryEntryRepository();
 
 		bind(DictionaryMetadataGateway.class).toInstance(new StardictDictionaryMetadataGateway(dictionaryFileFinder));
 		bind(DictionaryIndexGateway.class).toInstance(new StardictDictionaryIndexGateway(dictionaryFileFinder));
 
		bind(DictionaryRepository.class).toInstance(dictionaryRepository);
		bind(DictionaryEntryRepository.class).toInstance(verbaApplication.getDictionaryEntryRepository());
 		bind(CardSetDao.class).toInstance(verbaApplication.getCardSetDao());
 		bind(CardDao.class).toInstance(verbaApplication.getCardDao());
 
 		bind(NewDictionariesScanner.class).toInstance(
 				new NewStardictDictionariesScanner(getVerbaDirectory(), dictionaryRepository));
 		bind(SuggestionAdviser.class).toInstance(new SimpleSuggestionAdviser(entryRepository));
 		bind(PhraseLookup.class).toInstance(new StardictPhraseLookup(
 				dictionaryRepository, entryRepository, new StardictDictionaryDefinitionsGateway(dictionaryFileFinder)));
 	}
 
 }
