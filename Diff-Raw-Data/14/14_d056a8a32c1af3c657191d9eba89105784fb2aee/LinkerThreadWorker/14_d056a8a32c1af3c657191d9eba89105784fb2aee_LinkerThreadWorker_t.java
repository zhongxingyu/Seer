 package linker;
 
 import java.util.List;
 import java.util.Set;
 
 import linker.Linker.LinkedExtraction;
 import models.Extraction;
 import models.Item;
 import models.extractor.patch.Patch;
 import models.extractor.sourcecode.CodeRegion;
 import models.extractor.stacktrace.StackTrace;
 import db.Resources;
 
 public class LinkerThreadWorker implements Runnable
 {
 	Linker linker;
	private Item[] itemSet;
 	
 	public LinkerThreadWorker(Linker linker)
 	{		
 		this.linker = linker;
 	}
 	
 	public void initItems(Item[] itemSet)
 	{
 		this.itemSet = itemSet;
 	}
 	
 	public void run()
 	{
		Resources.log("Running new thread on : \n{");
 		for (Item item : itemSet) 
 		{
 			Resources.log("On item : " + item.getItemId());
 			List<Extraction> keys = linker.extractor.ExtractKeys(item);
 			for (Extraction extraction : keys)
 			{
 				Resources.log(extraction.getClass().toString());	
 				if (extraction instanceof StackTrace)
 				{
 					Set<LinkedExtraction> relevantCommitsByFiles = linker.GetRelevantCommitsForFiles(((StackTrace) extraction).getFilenames(), item.getItemDate());
 					for (LinkedExtraction linkedE : relevantCommitsByFiles)
 						linker.comDb.insertLink(new models.Link(item.getItemId(), linkedE.commit.getCommit_id(), linkedE.Confidence));
 				}
 				else if (extraction instanceof CodeRegion)
 				{
 					Set<LinkedExtraction> relevantCommitsBySnippet = linker.GetRelevantCommitsByCodeRegion((CodeRegion) extraction, item.getItemDate());
 					for (LinkedExtraction linkedE : relevantCommitsBySnippet)
 						linker.comDb.insertLink(new models.Link(item.getItemId(), linkedE.commit.getCommit_id(), linkedE.Confidence));
 				}
 				else if (extraction instanceof Patch)
 				{
 					Set<LinkedExtraction> relevantCommitsBySnippet = linker.GetRelevantCommitsByPatch((Patch) extraction, item.getItemDate());
 					for (LinkedExtraction linkedE : relevantCommitsBySnippet)
 						linker.comDb.insertLink(new models.Link(item.getItemId(), linkedE.commit.getCommit_id(), linkedE.Confidence));
 				}
 			}
 		}
 		Resources.log("}\n");
 	}
 }
