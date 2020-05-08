 package ui.wrapper;
 
 import java.util.ArrayList;
 
 import ui.activity.BaseActivity;
 import ui.factory.WrapperFactory;
 import config.Config;
 
 /**
  * A TabWrapper(s) Layout holds TabButtonWrapper(s).
  * They also hold references to the ContainerWrapper(s) that map to each tab
  *
  */
 public class TabWrapper extends ContainerWrapper {
 	
 	private int currentTabIndex;
 	private ArrayList<TabButtonWrapper> tabs;
 	
 	public TabWrapper(BaseActivity activity, ContainerWrapper parent, Config config) {
 		super(activity, parent, config);
 		tabs = new ArrayList<TabButtonWrapper>();
 	}
 	
 	@Override
 	public void createWrappers() {
 		super.createWrappers();
 		int tabCount = 0;
 		for(Wrapper childWrapper : childWrappers){
 			if(childWrapper instanceof TabButtonWrapper){
 				TabButtonWrapper tab = (TabButtonWrapper)childWrapper;
 				tab.setTabIndex(tabCount);
 				tabs.add(tab);
 				tabCount++;
 			}
 		}
 	}
 	
 	@Override
 	public void finializeWrappers(){
 		setCurrentTab(0);
 		super.finializeWrappers();
 	}
 	
 	private void setCurrentTab(int index) {
		getCurrentTabButtonWrapper().getButtonView().setSelected(false);
 		currentTabIndex = index;
 		getCurrentTabButtonWrapper().getButtonView().setSelected(true);
 		getCurrentTabButtonWrapper().setContainerWrapper(getCurrentTargetContainerWrapper());
 	}
 
 	@Override
 	public void updateData() {}
 
 	@Override
 	public void setText(String text) {}
 
 	public void setActiveTab(TabButtonWrapper tab) {
 		int index = tab.getTabIndex();
 		ContainerWrapper newWrapper = tab.getContainerWrapper();
 		
 		if(newWrapper == null)
 			newWrapper = initContainerWrapper(index);
 		else if(newWrapper == getCurrentTargetContainerWrapper())
 			return;
 		
 		boolean success = activity.replaceFragment(newWrapper, getCurrentTargetContainerWrapper());
 		
 		if(success){
 			parentWrapper.replaceChildWrapper(getCurrentTargetContainerWrapper(), newWrapper);
 			setCurrentTab(index);
 			activity.relayout(false);
 		}
 	}
 
 	private ContainerWrapper initContainerWrapper(int index) {
 		TabButtonWrapper tab = getTabButtonWrapper(index);
 		
 		ContainerWrapper wrapper = (ContainerWrapper) new WrapperFactory().createAndInitWrapper(activity, parentWrapper, config.targetWrapperIds.get(index));
 		
 		tab.setContainerWrapper(wrapper);
 		
 		return wrapper;
 	}
 	
 	protected TabButtonWrapper getCurrentTabButtonWrapper(){
 		return getTabButtonWrapper(currentTabIndex);
 	}
 	
 	protected TabButtonWrapper getTabButtonWrapper(int index) {
 		return tabs.get(index);
 	}
 	
 	protected ContainerWrapper getCurrentTargetContainerWrapper() {
 		return (ContainerWrapper)getActivity().getWrapperById(config.targetWrapperIds.get(currentTabIndex));
 	}
 
 
 }
