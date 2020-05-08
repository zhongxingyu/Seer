 /**
  * 
  */
 package pl.edu.pw.rso2012.a1.dvcs.model.repository;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.thoughtworks.xstream.XStream;
 
 import difflib.Patch;
 
 import pl.edu.pw.rso2012.a1.dvcs.controller.Controller;
 import pl.edu.pw.rso2012.a1.dvcs.controller.event.operation.request.PullRequestEvent;
 import pl.edu.pw.rso2012.a1.dvcs.model.changedata.ChangeData;
 import pl.edu.pw.rso2012.a1.dvcs.model.communication.Commit;
 import pl.edu.pw.rso2012.a1.dvcs.model.communication.MailMessage;
 import pl.edu.pw.rso2012.a1.dvcs.model.configuration.Configuration;
 import pl.edu.pw.rso2012.a1.dvcs.model.configuration.RepositoryConfiguration;
 import pl.edu.pw.rso2012.a1.dvcs.model.file.clock.LogicalClock;
 import pl.edu.pw.rso2012.a1.dvcs.model.file.clock.LogicalClock.CompareResult;
 import pl.edu.pw.rso2012.a1.dvcs.model.newdata.NewData;
 import pl.edu.pw.rso2012.a1.dvcs.model.operation.*;
 import pl.edu.pw.rso2012.a1.dvcs.model.workingcopy.WorkingCopy;
 
 /**
  * @author Grzegorz Sancewicz & Oskar Leszczynski & Darek
  * 
  */
 public class Repository {
 	private String absolutePath;
 	private final WorkingCopy workingCopy;
 	private final XStream xStream;
 
 	public Repository() {
 		RepositoryConfiguration repositoryConfiguration = Configuration
 				.getInstance().getRepositoryConfiguration();
 		this.absolutePath = repositoryConfiguration.getRepositoryAbsolutePath();
 		workingCopy = new WorkingCopy(
 				repositoryConfiguration.getRepositoryAddress(),
 				repositoryConfiguration.getRepositoryAbsolutePath());
 		this.xStream = new XStream();
 	}
 
 	public String getAbsolutePath() {
 		return absolutePath;
 	}
 
 	public void setAbsolutePath(final String absolutePath) {
 		this.absolutePath = absolutePath;
 	}
 
 	public CommitOperation commit(Set<String> filesToCommit) {
 		Map<String, ChangeData> diffResult = workingCopy.diffFiles(autoDelete(filesToCommit));
 		CommitOperation operation = new CommitOperation(diffResult);
 
 		// TODO sprawdzic czy workingCopy tworzy kopie bo chyba nie tworzy kopii
 		// z working copy
 
 		return operation;
 	}
 	
 	/**
 	 * Metoda wywoływana przed wykonaniem workingCopy.diffFiles()
 	 * Sprawdza, czy pliki z podawanej w commicie listy maja swoje fizycnze pokrycie na dysku
 	 */
 	public List<String> autoDelete(Set<String> filesToCommit){
 		
 		if (filesToCommit == null)
 		{
 			// domyślnie commitujemy wszystko, pobieramy liste plikow wersjonowanych z WC
 			// zakladam ze ta metoda zwraca pliki fizycznie występujące na dysku
 			List<String> fileList = workingCopy.getFileNames();
 			fileList = workingCopy.checkFileList(fileList);
 			
 			return fileList;
 		}
 		
 		// sprawdzamy czy pliki z filesToCommit maja pokrycie fizyczne jeśli nie to usuwamy je z tej listy
 		ArrayList<String> toDelete = new ArrayList<String>();
 		List<String> ourFiles= workingCopy.getFileNames();
 		List<String> checkedOurFiles = workingCopy.checkFileList(ourFiles);
 		List<String> finalList = new ArrayList<String>();
 		
 		//n^2
 		for (String filename : ourFiles)
 		{
 			if (!checkedOurFiles.contains(filename))
 			{
 				ourFiles.remove(filename);
 				workingCopy.deleteFile(filename);
 			}
 		}
 		
 		// n^1
 		for (String filename : ourFiles)
 		{
 			if (filesToCommit.contains(filename))
 			{
 				finalList.add(filename);
 			}
 		}
 		
 		//n^2 + n - poza tym algorytm dzialania jest błędny, bo usuwa pliki, które nie są w danym commicie a nie o to chodziło
 //		for (String fileName : filesToCommit)
 //		{
 //			if (!ourFiles.contains(fileName))
 //			{
 //				toDelete.add(fileName);
 //			}
 //		}
 //
 //		for (String fileName : toDelete)
 //			filesToCommit.remove(fileName);
 		
 		return finalList;
 	}
 
 	public CloneRequestOperation cloneRequest() {
 		return new CloneRequestOperation(this.getWorkingCopy().getAddress());
 	}
 
 	public CloneOperation prepareClone(final List<Commit> commitList) {
 		return new CloneOperation(commitList, workingCopy.getAddress());
 	}
 
 	public void clone(final Controller controller, CloneOperation operation)
 			throws InterruptedException {
 		// Odtwarzanie repozytorium
 		List<Commit> commitList = operation.getCommitList();
 		List<ChangeData> changeList = prepareChangeList(commitList);
 		String body;
 		MailMessage message;
 
 		// TODO Trzeba dodac do MailBox metode setCommits(List<Commit>)
 		// FIXME: rozwiazanie tymczasowe do poprawki jak bedzie metoda
 		// ustalone z Grzeskiem, ze zostaje jak jest
 		for (Commit commit : commitList) {
 			body = OperationToXML(commit.getCommitOperation());
 			message = new MailMessage(workingCopy.getAddress(), "commit "
 					+ commit.getRevision(), body);
 			controller.getModel().getMailbox().putMessage(message);
 		}
 
 		if (!changeList.isEmpty())
 			workingCopy.recoverFiles(changeList);
 	}
 
 	public PushResponseOperation push(final Controller controller,
 			PushOperation operation) {
 		Map<String, NewData> map = operation.getMap();
 
 		String result = mergeData(map);
 		PushResponseOperation opResult = new PushResponseOperation(result);
 		return opResult;
 	}
 
 	public PushOperation preparePush(final Map<String, NewData> data) {
 		PushOperation op = new PushOperation(data);
 		return op;
 	}
 
 	public PullResponseOperation pull(final Controller controller,
 			PullOperation operation) {
 
 		Map<String, NewData> map = operation.getData();
 
 		String result = mergeData(map);
 		PullResponseOperation opResult = new PullResponseOperation(result);
 		return opResult;
 	}
 
 	public PullOperation preparePull() {
 		PullOperation operation = new PullOperation(
 				workingCopy.getSnapshotFiles(workingCopy.getFileNames()));
 		return operation;
 	}
 
 	public PushRequestOperation pushRequest() {
 		List<String> files = workingCopy.getFileNames();
 		Map<String, NewData> data = workingCopy.getSnapshotFiles(files);
 		PushRequestOperation operation = new PushRequestOperation(data,
 				workingCopy.getAddress());
 		return operation;
 	}
 
 	public PullRequestOperation pullRequest() {
 		return new PullRequestOperation(workingCopy.getAddress());
 	}
 
 	public void update(List<Commit> commitList) {
 		List<ChangeData> changeList = prepareChangeList(commitList);
 
 		if (!changeList.isEmpty())
 			workingCopy.recoverFiles(changeList);
 	}
 
 	public void add(final ArrayList<String> fileList) {
 		workingCopy.addFiles(fileList);
 	}
 
 	public void delete(final ArrayList<String> fileList) {
 		workingCopy.deleteFiles(fileList);
 	}
 
 	public WorkingCopy getWorkingCopy() {
 		return workingCopy;
 	}
 
 	protected List<ChangeData> prepareChangeList(final List<Commit> commitList) {
 		List<ChangeData> changeList = new ArrayList<ChangeData>();
 		if (commitList != null && !commitList.isEmpty()) {
 			CommitOperation operation;
 			Collections.sort(commitList);
 			Map<String, ChangeData> map;
 			Map<String, ChangeData> finalMap, lastMap;
 
 			ChangeData data, finalData;
 
 			Commit lastCommit = commitList.get(commitList.size() - 1);
 			finalMap = new HashMap<String, ChangeData>();
 			lastMap = lastCommit.getCommitOperation().getFilesDiffs();
 			Set<String> fileSet = lastMap.keySet();
 
 			// niebezpieczne kopiowanie, brak deep copy, wykonuje kopie
 			// referencji
 			// inicjalizacja nowej mapy wynikowej
 			// z pusta lista diffow
 			for (String filename : fileSet) {
 				finalData = new ChangeData(filename);
 				finalData.setVector(lastMap.get(filename).getVector());
 				finalMap.put(filename, finalData);
 			}
 
			for (int i = 0; i < commitList.size(); ++i) {
 				map = commitList.get(i).getCommitOperation().getFilesDiffs();
 
				for (String filename : fileSet) {
 					data = map.get(filename);
					finalData = map.get(filename);
 					if (data != null)
 						finalData.addDiffToList(data.getDifflist());
 					else
 						finalData.clearDiffList();
 				}
 			}
 
 			changeList.addAll(finalMap.values());
 
 			return changeList;
 		} else {
 			// TODO zastanowic sie czy dla null nie powinno rzucac wyjatkiem
 			return changeList;
 		}
 	}
 
 	public String OperationToXML(AbstractOperation operation) {
 		return xStream.toXML(operation);
 	}
 
 	public AbstractOperation OperationFromXML(String xml) {
 		return (AbstractOperation) xStream.fromXML(xml);
 	}
 
 	// zwraca komunikat ktory bedzie wyswietlony na gui
 	// tzn zawierajacy liste plikow z konfliktami
 	protected String mergeData(Map<String, NewData> map) {
 		NewData newFileDescriptorTmp;
 		NewData ourFileDescriptorTmp;
 		Map<String, NewData> ourData = workingCopy.getSnapshotFiles(workingCopy
 				.getFileNames());
 		Map<String, NewData> filesForReplaceFilesMethod = new HashMap<String, NewData>(); // przechowuje
 																							// pliki
 																							// do
 																							// dodania
 																							// i
 																							// calkowitej
 																							// podmiany
 		Map<String, NewData> conflictedFiles = new HashMap<String, NewData>();
 
 		for (String fileName : map.keySet()) {
 			newFileDescriptorTmp = map.get(fileName);
 
 			// sprawdzamy czy plik u nas istnieje, jak nie to go dodajemy
 			if (ourData.containsKey(fileName)) {
 				ourFileDescriptorTmp = ourData.get(fileName);
 				CompareResult compareResult = ourFileDescriptorTmp
 						.getLogicalClock().compare(
 								newFileDescriptorTmp.getLogicalClock());
 
 				switch (compareResult) {
 
 				case EQUAL:
 					// wersje plikow takie same, sprawdzamy dodatkowo zawartosc
 					if (workingCopy.isFileDifferent(
 							newFileDescriptorTmp.getFileContent(), fileName))
 						conflictedFiles.put(fileName, newFileDescriptorTmp); // konflikt
 					else {
 						// pliki maja te sama wersje i ta sama zawartosc, nic
 						// nie robimy
 					}
 					break;
 
 				case GREATER:
 					// plik z zewnatrz jest nowsza wersja naszego, podmieniamy
 					// caly plik
 					filesForReplaceFilesMethod.put(fileName,
 							newFileDescriptorTmp);
 					break;
 
 				case LESS:
 					// plik z zewnątrz jest starszą wersją naszego, nic nie
 					// robimy bo dążymy do posiadania najnowszych wersji
 					break;
 
 				case UNKNOWN:
 					// nie da się określić który plik jest nowszy, badamy
 					// zawartosc
 					if (workingCopy.isFileDifferent(
 							newFileDescriptorTmp.getFileContent(), fileName)) {
 						conflictedFiles.put(fileName, newFileDescriptorTmp); // pliki rozne i nie wiemy ktory nowszy = konflikt
 					} else {
 						// pliki takie same zawartosciowo ale nie wiemy ktory
 						// jest nowszy, ignorujemy
 					}
 					break;
 				}
 
 			} else
 				filesForReplaceFilesMethod.put(fileName, newFileDescriptorTmp); // dodajemy nieistniejacy plik
 		}
 
 		workingCopy.setWorkingFiles(filesForReplaceFilesMethod);
 		// workingCopy.replaceFiles(filesForReplaceFilesMethod);
 		workingCopy.mergeConflictedFiles(conflictedFiles);
 
 		String resultConflictedFiles = "";
 		for (String file : conflictedFiles.keySet()) {
 			resultConflictedFiles += file + "\n";
 		}
 
 		return resultConflictedFiles;
 		// return null;
 	}
 }
