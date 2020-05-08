 package org.codefaces.core.models;
 
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IPath;
 
 public class RepoProject extends RepoFolder {
 	private static final String AT = "@";
 
 	private static final String ROOT = "root";
 
 	private static String buildProjectName(RepoResource resource) {
 		StringBuilder nameBuilder = new StringBuilder();
 
 		nameBuilder.append(isRoot(resource) ? ROOT : resource.getPath()
 				.toString());
 		nameBuilder.append(AT);
 		nameBuilder.append(getRepo(resource).getUrl());
 
 		return nameBuilder.toString();
 	}
 
 	private static Repo getRepo(RepoResource resource) {
 		return resource.getRoot().getRepo();
 	}
 
 	private static boolean isRoot(RepoResource resource) {
 		return resource.getType() == RepoResourceType.FOLDER_ROOT;
 	}
 
 	private final RepoFolder wrappedfolder;
 
 	public RepoProject(RepoFolder wrappedfolder) {
		super(null, null, wrappedfolder.getId(), wrappedfolder.getName(),
				RepoResourceType.PROJECT);
 		this.wrappedfolder = wrappedfolder;
 		this.id = this.name = buildProjectName(wrappedfolder);
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		return super.getAdapter(adapter);
 	}
 
 	@Override
 	public Collection<RepoResource> getChildren() {
 		return getWrappedFolder().getChildren();
 	}
 
 	@Override
 	protected RepoFolderInfo getInfo() {
 		return getWrappedFolder().getInfo();
 	}
 
 	@Override
 	public RepoResource getParent() {
 		return getWrappedFolder().getParent();
 	}
 
 	@Override
 	public IPath getPath() {
 		return getWrappedFolder().getPath();
 	}
 
 	@Override
 	public Object getProperty(String key) {
 		return getWrappedFolder().getProperty(key);
 	}
 
 	@Override
 	public RepoFolderRoot getRoot() {
 		return getWrappedFolder().getRoot();
 	}
 
 	private RepoFolder getWrappedFolder() {
 		return wrappedfolder;
 	}
 
 	@Override
 	public boolean hasChildren() {
 		return getWrappedFolder().hasChildren();
 	}
 
 	@Override
 	public void setProperty(String key, Object value) {
 		getWrappedFolder().setProperty(key, value);
 	}
 }
