 package com.neuro_immune_detector_core.services;
 
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.neuro_immune_detector_core.helpers.RandomHelper;
 import com.neuro_immune_detector_core.repositories.FileRepository;
 
 
 public class FileExtractorImpl implements FileExtractor {
 
 	@Override
 	public Collection<File> getRandomFiles(FileRepository repository,
 			int numberOfFiles) {
 		if (repository.size() < numberOfFiles) {
 			throw new IllegalArgumentException("Repository's size is less than requested numberOfFiles");
 		}
 		Set<File> res = new HashSet<File>(numberOfFiles); 
 	    int repositorySize = repository.size();
 	    for(int i = repositorySize - numberOfFiles; i < repositorySize; i++) {
	        int pos = RandomHelper.getRandomValue(i + 1);
 	        File file = repository.get(pos);
 	        if (res.contains(file)) {
 	            res.add(repository.get(i));
 	        }
 	        else {
 	            res.add(file);
 	        }
 	    }
 	    return Collections.unmodifiableCollection(res);
 	}
 }
