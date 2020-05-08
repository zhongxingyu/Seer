 package il.technion.ewolf.socialfs;
 
 import il.technion.ewolf.kbr.Key;
 import il.technion.ewolf.kbr.KeyFactory;
 import il.technion.ewolf.stash.Group;
 import il.technion.ewolf.stash.LazyChunkDecryptor;
 import il.technion.ewolf.stash.Stash;
 import il.technion.ewolf.stash.crypto.Signable;
 import il.technion.ewolf.stash.crypto.SignatureOutputStream;
 import il.technion.ewolf.stash.exception.CouldNotDecryptException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 public class SFSFile extends Signable implements KeyHolder {
 
 	private static final long serialVersionUID = 5787686277471955577L;
 	
 	private transient Stash stash = null;
 	private transient Cache<SFSFile> fileCache = null;
 	
 	private Key groupId;
 	private String name;
 	private Key fileKey;
 	private Key parentKey;
 	private Serializable data = null;
 	private Key[] subFilesKeys = null;
 	
 	
 	private boolean isModifiable = true;
 	
 	SFSFile() {
 	}
 	
 	@Inject
 	SFSFile(Stash stash,
 			@Named("socialfs.cache.filecache") Cache<SFSFile> fileCache) {
 		this.stash = stash;
 		this.fileCache = fileCache;
 	}
 	
 	SFSFile setTransientParams(Stash stash, Cache<SFSFile> fileCache) {
 		this.stash = stash;
 		this.fileCache = fileCache;
 		return this;
 	}
 	
 	private SFSFile walkFS(SFSFile curr, String targetName) throws FileNotFoundException {
 		
 		targetName = targetName.replaceAll("^[/]*", "");
 		//System.out.println("targetName: "+targetName);
 		if (targetName.isEmpty())
 			return curr;
 		
 		String name = targetName.split("/")[0];
 		targetName = targetName.substring(name.length());
 		
 		int i=0;
 		while (i < subFilesKeys.length) {
 			try {
 				SFSFile subFile = curr.getSubFiles(i);
 				if (subFile.getName().equals(name))
 					return walkFS(subFile, targetName);
 				
 			} catch (FileNotFoundException e) {
 				throw new FileNotFoundException();
 			} catch (Exception e) {
 				
 			}
 			++i;
 		}
 		
 		throw new FileNotFoundException();
 	}
 	
 	public SFSFile getSubFile(String name) throws FileNotFoundException {
 		return walkFS(this, name);
 	}
 	
 	@Override
 	protected void updateSignature(Signature sig) throws SignatureException {
 		sig.update(name.getBytes());
 		sig.update(fileKey.getBytes());
 		sig.update(parentKey.getBytes());
 		
 		SignatureOutputStream sigOut = new SignatureOutputStream(sig);
 		ObjectOutputStream oout = null;
 		try {
 			oout = new ObjectOutputStream(sigOut);
 			oout.writeObject(data);
 		} catch (IOException e) {
 			throw new SignatureException(e);
 		} finally {
 			try { oout.close(); } catch (Exception e) {}
 			try { sigOut.close(); } catch (Exception e) {}
 		}
 		
 		for (Key k : subFilesKeys)
 			sig.update(k.getBytes());
 	}
 	
 	
 	SFSFile setFileKey(Key fileKey) {
 		if (!isModifiable)
 			throw new IllegalStateException();
 		
 		this.fileKey = fileKey;
 		return this;
 	}
 	
 	SFSFile setParentKey(Key parentKey) {
 		if (!isModifiable)
 			throw new IllegalStateException();
 		
 		this.parentKey = parentKey;
 		return this;
 	}
 	
 	
 	SFSFile setNrSubFiles(int n, KeyFactory keyFactory) {
 		if (subFilesKeys != null)
 			throw new IllegalStateException("already set");
 		
 		if (!isModifiable)
 			throw new IllegalStateException();
 		
 		subFilesKeys = new Key[n];
 		for (int i=0; i < n; ++i) {
 			subFilesKeys[i] = keyFactory.generate();
 		}
 		return this;
 	}
 	
 	SFSFile setGroupId(Key groupId) {
 		if (!isModifiable)
 			throw new IllegalStateException();
 		
 		this.groupId = groupId;
 		return this;
 	}
 	
 	public SFSFile setData(Serializable data) {
 		if (!isModifiable)
 			throw new IllegalStateException();
 		
 		this.data = data;
 		return this;
 	}
 	
 	public SFSFile setName(String name) {
 		if (!isModifiable)
 			throw new IllegalStateException();
 		
 		this.name = name;
 		return this;
 	}
 	
 	public Key getGroupId() {
 		return groupId;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public Serializable getData() {
 		return data;
 	}
 	
 	private boolean isSubFile(SFSFile f, int index) {
 		if (f == null)
 			return false;
 		
 		if (!getPubSigKey().equals(f.getPubSigKey()))
 			return false;
 		
 		if (!subFilesKeys[index].equals(f.getFileKey()) || !getFileKey().equals(f.parentKey))
 			return false;
 		
 		return true;
 	}
 	
 	public SFSFile getSubFiles(int index) throws FileNotFoundException, CouldNotDecryptException {
 		if (index >= subFilesKeys.length)
 			throw new FileNotFoundException();
 		
 		Key k = subFilesKeys[index];
 		
 		SFSFile cachedResults = fileCache.search(k);
 		if (isSubFile(cachedResults, index))
 			return cachedResults;
 		
 		List<LazyChunkDecryptor> files = stash.get(k);
 		if (files.isEmpty())
 			throw new FileNotFoundException();
 		
 		for (LazyChunkDecryptor p : files) {
 			try {
 				SFSFile f = p.downloadAndDecrypt(SFSFile.class);
 				
 				if (!isSubFile(f, index))
 					continue;
 				
 				return f.setTransientParams(stash, fileCache);
 				
 			} catch (Exception e) {
 				System.err.println("could not decrypt chunk, move on to the next");
 				//e.printStackTrace();
 			}
 		}
 		throw new CouldNotDecryptException();
 	}
 	
 	public void append(SFSFile f, Group group) {
 		if (!f.getPubSigKey().equals(getPubSigKey()) || getPrvSigKey() == null)
 			throw new UnsupportedOperationException("file is not appendable");
 		
 		int i=0;
 		while (i < subFilesKeys.length) {
 			try {
 				getSubFiles(i);
 				++i;
 			} catch (FileNotFoundException e) {
 				break;
 			} catch (CouldNotDecryptException e) {
 				break;
 			}
 			
 		}
 		// TODO: check i < subFilesKeys.length
 		// and append a keys chunk if so
 		
 		f.setFileKey(subFilesKeys[i])
 			.setParentKey(fileKey)
 			.setGroupId(group.getGroupId())
 			.setPrvSigKey(getPrvSigKey());
 
 		f.setUnmodifiable();
 		
 		stash.put(f.fileKey, f, group);
 		fileCache.insert(f);
 	}
 	
 	@Override
 	public int hashCode() {
 		return groupId.hashCode() + name.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null || !(getClass().equals(obj.getClass())))
 			return false;
 		SFSFile f = (SFSFile)obj;
 		
 		return getName().equals(f.getName()) &&
 				(data == null ? f.getData() == null : data.equals(f.getData())) &&
 				groupId.equals(f.getGroupId()) &&
 				fileKey.equals(f.fileKey) &&
 				parentKey.equals(f.parentKey) &&
 				Arrays.equals(subFilesKeys, f.subFilesKeys);
 				
 	}
 	
 	@Override
 	public String toString() {
 		return "name: "+getName()+"\n\n" +
 				"subFilesKeys: "+Arrays.asList(subFilesKeys);
 	}
 
 	public List<SFSFile> list() {
 		List<SFSFile> $ = new ArrayList<SFSFile>();
 		int i=0;
 		while (i < subFilesKeys.length) {
 			try {
				$.add(getSubFiles(i++));
 			} catch (FileNotFoundException e) {
 				break;
 			} catch (CouldNotDecryptException e) {
 				continue;
 			}
 		}
 		return $;
 	}
 
 	public Key getFileKey() {
 		return fileKey;
 	}
 	
 	SFSFile setUnmodifiable() {
 		isModifiable = false;
 		return this;
 	}
 	
 	@Override
 	public Key getKey() {
 		return getFileKey();
 	}
 	
 }
