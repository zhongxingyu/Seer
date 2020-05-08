 package jie.android.el;
 
 import java.util.Stack;
 
 import jie.android.el.fragment.AboutFragment;
 import jie.android.el.fragment.BaseFragment;
 import jie.android.el.fragment.DictionaryFragment;
 import jie.android.el.fragment.DownloadFragment;
 import jie.android.el.fragment.ListFragment;
 import jie.android.el.fragment.MemoryFragment;
 import jie.android.el.fragment.SettingFragment;
 import jie.android.el.fragment.ShowFragment;
 import jie.android.el.fragment.VocabFragment;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
 public class FragmentSwitcher {
 
 	public interface OnSwitchListener {
 		public void onSwitch(Type oldType, Type newType);
 	}
 
 	public enum Type {
 
 		LIST("list", false), SHOW("show", false), ABOUT("about", true), SETTING("setting", true), DOWNLOAD("download", true), DICTIONARY("dictionary", true), VOCAB(
 				"vocab", true), MEMORY("memory", true);
 
 		private final String title;
 		private final boolean removed;
 
 		private Type(final String title, boolean removed) {
 			this.title = title;
 			this.removed = removed;
 		}
 
 		public String getTitle() {
 			return title;
 		}
 
 		public boolean hasRemoved() {
 			return removed;
 		}
 
 		public static Type getType(final String title) {
 			if (title.equals(LIST.getTitle())) {
 				return LIST;
 			} else if (title.equals(SHOW.getTitle())) {
 				return SHOW;
 			} else {
 				return null;
 			}
 		}
 	}
 
 	private final ELActivity activity;
 	private FragmentManager fragmentManager = null;
 	private Type curType = null;
 	private OnSwitchListener onSwitchListener = null;
 
 	private Stack<Type> stackType = new Stack<Type>();
 
 	public FragmentSwitcher(final ELActivity activity) {
 		this.activity = activity;
 		fragmentManager = this.activity.getSupportFragmentManager();
 	}
 
	public boolean show(Type type) {
		return show(type, null);
	}

 	public boolean show(Type type, Bundle args) {
 		if (type == null) {
 			return false;
 		}
 
 		if (curType == type) {
 			BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
 			if (fragment != null) {
 				fragment.onArguments(args);
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		boolean found = false;
 		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
 		if (fragment == null) {
 			fragment = create(type);
 			if (fragment == null) {
 				return false;
 			}
 			found = false;
 		} else {
 			found = true;
 		}
 
 		FragmentTransaction ft = fragmentManager.beginTransaction();
 		if (curType != null) {
 			if (!curType.hasRemoved()) {
 				hideFragment(ft, curType);
 			} else {
 				removeFragment(ft, curType);
 			}
 
 			if (found) {
 				ft.show(fragment);
 			} else {
 				ft.add(R.id.main, fragment, type.getTitle());
 			}
 
 			// if (found) {
 			// if (!curType.hasRemoved()) {
 			// hideFragment(ft, curType);
 			// } else {
 			// removeFragment(ft, curType);
 			// }
 			// ft.show(fragment);
 			// } else {
 			// if (!curType.hasRemoved()) {
 			// hideFragment(ft, curType);
 			// ft.add(R.id.main, fragment, type.getTitle());
 			// } else {
 			// ft.replace(R.id.main, fragment, type.getTitle());
 			// }
 			// }
 
 		} else {
 			ft.add(R.id.main, fragment, type.getTitle());
 		}
 
 		ft.commit();
 
 		fragment.onArguments(args);
 
 		if (onSwitchListener != null) {
 			onSwitchListener.onSwitch(curType, type);
 		}
 
 		curType = type;
 
 		return true;
 	}
 
 	private void removeFragment(FragmentTransaction ft, Type type) {
 		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
 		if (fragment != null) {
 			ft.remove(fragment);
 		}
 	}
 
 	private void hideFragment(FragmentTransaction ft, Type type) {
 		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
 		if (fragment != null) {
 			ft.hide(fragment);
 			stackType.push(type);
 		}
 	}
 
 	private BaseFragment create(Type type) {
 		BaseFragment fragment = null;
 		switch (type) {
 		case LIST:
 			fragment = new ListFragment();
 			break;
 		case SHOW:
 			fragment = new ShowFragment();
 			break;
 		case DOWNLOAD:
 			fragment = new DownloadFragment();
 			break;
 		case SETTING:
 			fragment = new SettingFragment();
 			break;
 		case ABOUT:
 			fragment = new AboutFragment();
 			break;
 		case DICTIONARY:
 			fragment = new DictionaryFragment();
 			break;
 		case VOCAB:
 			fragment = new VocabFragment();
 			break;
 		case MEMORY:
 			fragment = new MemoryFragment();
 			break;
 		default:
 			return null;
 		}
 		return fragment;
 	}
 
 	public BaseFragment getFragment() {
 		if (curType == null) {
 			return null;
 		}
 		return (BaseFragment) fragmentManager.findFragmentByTag(curType.getTitle());
 	}
 
 	public Type getCurrentType() {
 		return curType;
 	}
 
 	public boolean isRemovedType() {
 		if (curType == null)
 			return false;
 		return curType.hasRemoved();
 	}
 
 	public boolean showPrevFragment() {
 		if (stackType.size() > 0) {
 			Type type = stackType.pop();
 			if (type != null) {
				show(type);
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void setOnSwitchListener(OnSwitchListener listener) {
 		onSwitchListener = listener;
 	}
 
 	public void restore() {
 		if (curType != null) {
 			if (curType.hasRemoved()) {
 				showPrevFragment();
 			}
 		}
 	}
 }
