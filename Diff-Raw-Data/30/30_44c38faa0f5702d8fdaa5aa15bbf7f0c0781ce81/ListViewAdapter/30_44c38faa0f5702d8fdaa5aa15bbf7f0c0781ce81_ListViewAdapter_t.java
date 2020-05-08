 package org.kvj.sierra5.ui.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.kvj.bravo7.format.PlainTextFormatter;
 import org.kvj.bravo7.format.TextFormatter;
 import org.kvj.sierra5.App;
 import org.kvj.sierra5.R;
 import org.kvj.sierra5.common.data.Node;
 import org.kvj.sierra5.common.plugin.FormatSpan;
 import org.kvj.sierra5.common.plugin.Plugin;
 import org.kvj.sierra5.common.plugin.PluginInfo;
 import org.kvj.sierra5.common.theme.Theme;
 import org.kvj.sierra5.data.Controller;
 import org.kvj.sierra5.ui.adapter.theme.ThemeProvider;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListAdapter;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 
 public class ListViewAdapter implements ListAdapter {
 
 	public static interface ListViewAdapterListener {
 		public void itemSelected(int selected);
 	}
 
 	private static final String TAG = "Adapter";
 
 	private Node root = null;
 	private boolean showRoot = false;
 	private DataSetObserver observer = null;
 	private int selectedIndex = -1;
 	private ListViewAdapterListener listener = null;
 	private int textSize = 0;
 	DefaultTextFormatter defaultTextFormatter = new DefaultTextFormatter();
 	private PlainTextFormatter<Node> textFormatter = null;
 	private Map<String, RemoteViews> remoteRenders = new HashMap<String, RemoteViews>();
 	List<Plugin> remoteRenderPlugins = new ArrayList<Plugin>();
 
 	private Theme theme = null;
 
 	private Controller controller = null;
 
 	class DefaultTextFormatter implements NodeTextFormatter {
 
 		@Override
 		public Pattern getPattern(Node note, boolean selected) {
 			if (Node.TYPE_TEXT == note.type) { // Text - skip
 				return null;
 			}
 			return TextFormatter.eatAll;
 		}
 
 		@Override
 		public void format(Node note, SpannableStringBuilder sb, Matcher m,
 				String text, boolean selected) {
 			int color = Node.TYPE_FOLDER == note.type ? theme.cbLYellow
 					: theme.ccLBlue;
 			PlainTextFormatter.addSpan(sb, m.group(0), new ForegroundColorSpan(
 					color));
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public ListViewAdapter(ListViewAdapterListener listener, Theme theme) {
 		this.listener = listener;
 		this.theme = theme;
 		textSize = App.getInstance().getIntPreference(R.string.docFont,
 				R.string.docFontDefault);
 
 		textFormatter = new PlainTextFormatter<Node>(defaultTextFormatter);
 	}
 
 	private static class SearchInTreeResult {
 		int size = 1;
 		Node foundNode = null;
 	}
 
 	private SearchInTreeResult moveThru(Node node, int searchIndex) {
 		SearchInTreeResult result = new SearchInTreeResult();
 		if (searchIndex == 0) { // That's what we are looking for
 			result.foundNode = node;
 		}
 		if (node.collapsed) { // Collapsed - no children
 			return result;
 		}
 		if (null == node.children) { // No children
 			Log.w(TAG, "Expanded wout children: " + node.text);
 			return result;
 		}
		synchronized (node.children) { // Lock modifications
			for (Node child : node.children) { // Every children
				SearchInTreeResult r = moveThru(child, searchIndex
						- result.size);
				if (null != r.foundNode) { // Node found
					return r; // Don't need to go thru anymore
				}
				result.size += r.size;
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public int getCount() {
 		if (null == root) { // Root not created yet
 			return 0;
 		}
 		if (!showRoot && root.collapsed) { // No data to show
 			return 0; // No data visible
 		}
 		SearchInTreeResult r = moveThru(root, -1);
 		int result = r.size;
 		if (!showRoot) { // Root is not shown - decrease
 			result--;
 		}
 		return result;
 	}
 
 	@Override
 	public Node getItem(int index) {
 		if (-1 == index) { // Out of bounds
 			return null;
 		}
 		SearchInTreeResult r = moveThru(root, showRoot ? index : index + 1);
 		return r.foundNode;
 	}
 
 	@Override
 	public long getItemId(int index) {
 		return index;
 	}
 
 	@Override
 	public int getItemViewType(int index) {
 		return 0;
 	}
 
 	public void customize(View view, Node node, boolean selected) {
 		TextView textView = (TextView) view
 				.findViewById(R.id.listview_item_text);
 		// ImageView menuIcon = (ImageView) view
 		// .findViewById(R.id.listview_item_menu_icon);
 		// menuIcon.setVisibility(selected ? View.VISIBLE : View.GONE);
 		textView.setTextColor(theme.colorText);
 		// textView.setBackgroundColor(theme.colorBackground);
 		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 		SpannableStringBuilder text = new SpannableStringBuilder();
 		for (int i = 0; i < node.level; i++) { // Append level chars first
 			text.append(" ");
 		}
 		textFormatter.writePlainText(node, text, theme.colorText, node.text,
 				selected);
 		if (node.collapsed) {
 			text.setSpan(new StyleSpan(Typeface.ITALIC), 0, text.length(), 0);
 		}
 		if (selected) {
 			text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
 		}
 		textView.setText(text);
 	}
 
 	@Override
 	public View getView(int index, View view, ViewGroup parent) {
 		Node node = getItem(index);
 		if (null == node) { // Error case
 			return null;
 		}
 		if (view == null) {
 			LayoutInflater inflater = (LayoutInflater) parent.getContext()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.listview_item, parent, false);
 		}
 		ViewGroup root = (ViewGroup) view; // Actualy linear layout
 		while (root.getChildCount() > 1) { // Remove remote
 			root.removeViewAt(1);
 		}
 		boolean selected = selectedIndex == index;
 		view.setMinimumHeight((int) (28 * view.getContext().getResources()
 				.getDisplayMetrics().density));
 		view.findViewById(R.id.listview_item_menu_icon).setVisibility(
 				selected ? View.VISIBLE : View.GONE);
 		view.findViewById(R.id.listview_item_top).setVisibility(View.VISIBLE);
 		customize(view, node, index == selectedIndex);
 		if (Node.TYPE_TEXT == node.type) {
 			// Remote render only for text
 			synchronized (remoteRenders) { // Lock for modifications
 				RemoteViews rv = remoteRenders.get(node.text);
 				if (null != rv) { // Have remote views
 					Log.i(TAG, "getView: " + node.text + ", remote found");
 					View renderResult = rv.apply(parent.getContext(), root);
 					root.addView(renderResult);
 					if (!selected) { // Hide top part when not selected
 						view.findViewById(R.id.listview_item_top)
 								.setVisibility(View.GONE);
 					}
 				} else {
 					if (!remoteRenders.containsKey(node.text)) {
 						// Not started yet - start
 						remoteRender(node, root);
 					}
 				}
 			}
 		}
 		return view;
 	}
 
 	private void remoteRender(final Node node, final View parent) {
 		synchronized (remoteRenders) { // Lock
 			remoteRenders.put(node.text, null); // No remote render at start
 		}
 		AsyncTask<Void, Void, RemoteViews> task = new AsyncTask<Void, Void, RemoteViews>() {
 
 			@Override
 			protected RemoteViews doInBackground(Void... params) {
 				for (Plugin plugin : remoteRenderPlugins) { // every plugin
 					RemoteViews res = null;
 					try { // Remote error
 						res = plugin.render(node, theme, parent.getWidth());
 					} catch (Exception e) {
 					}
 					if (null != res) { // Got remote view
 						return res;
 					}
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(RemoteViews result) {
 				if (null != result) { // Got remote view
 					synchronized (remoteRenders) { // Lock
 						remoteRenders.put(node.text, result);
 					}
 					dataChanged();
 				}
 			}
 		};
 		task.execute();
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return getCount() == 0;
 	}
 
 	@Override
 	public void registerDataSetObserver(DataSetObserver observer) {
 		this.observer = observer;
 	}
 
 	@Override
 	public void unregisterDataSetObserver(DataSetObserver observer) {
 		if (null != observer && observer.equals(this.observer)) { // De-register
 			this.observer = null;
 		}
 	}
 
 	@Override
 	public boolean areAllItemsEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled(int arg0) {
 		return true;
 	}
 
 	public void dataChanged() {
 		if (null != observer) { // Have observer
 			observer.onChanged();
 		}
 	}
 
 	public void setController(Controller controller) {
 		this.controller = controller;
 		List<NodeTextFormatter> formatters = new ArrayList<NodeTextFormatter>();
 		List<Plugin> plugins = controller
 				.getPlugins(PluginInfo.PLUGIN_CAN_FORMAT);
 		// Log.i(TAG, "setController: plugins: " + plugins.size());
 		for (Plugin plugin : plugins) { // Create formatters for every plugin
 			formatters.addAll(formattersFromPlugin(plugin));
 		}
 		formatters.add(defaultTextFormatter);
 		textFormatter.setFormatters(formatters
 				.toArray(new NodeTextFormatter[0]));
 		resetRemoteRenders();
 	}
 
 	public boolean setRoot(Node node, boolean showRoot) {
 		if (node == null) { // Invalid node
 			return false;
 		}
 		root = node;
 		this.showRoot = showRoot;
 		return true;
 	}
 
 	public Node getRoot() {
 		return root;
 	}
 
 	public int getSelectedIndex() {
 		return selectedIndex;
 	}
 
 	public void setSelectedIndex(int index) {
 		selectedIndex = index;
 		if (null != listener) { // Have listener
 			listener.itemSelected(selectedIndex);
 		}
 	}
 
 	public boolean isShowRoot() {
 		return showRoot;
 	}
 
 	public static RemoteViews renderRemote(Controller controller, Node node,
 			String left, String themeName) {
 		RemoteViews result = new RemoteViews(
 				App.getInstance().getPackageName(), R.layout.listview_item);
 		Theme theme = ThemeProvider.getTheme(themeName);
 		ListViewAdapter instance = new ListViewAdapter(null, theme);
 		instance.setController(controller);
 		SpannableStringBuilder text = new SpannableStringBuilder();
 		if (null != left) { // Have left
 			text.append(left);
 		} else {
 			for (int i = 0; i < node.level; i++) { // Append level chars first
 				text.append(" ");
 			}
 		}
 		instance.textFormatter.writePlainText(node, text, theme.colorText,
 				node.text, false);
 		result.setViewVisibility(R.id.listview_item_menu_icon, View.GONE);
 		result.setTextViewText(R.id.listview_item_text, text);
 		return result;
 	}
 
 	class PluginFormatter implements NodeTextFormatter {
 
 		private Plugin plugin;
 		private int index;
 
 		public PluginFormatter(Plugin plugin, int index) {
 			this.plugin = plugin;
 			this.index = index;
 		}
 
 		@Override
 		public Pattern getPattern(Node note, boolean selected) {
 			try { // Remote errors
 				String pattern = plugin.getPattern(index, note, selected);
 				// Log.i(TAG, "getPattern: " + note.text + ", " + pattern);
 				if (null == pattern) { // Not needed
 					return null;
 				}
 				return Pattern.compile(pattern);
 			} catch (Exception e) {
 				// Log.w(TAG, "Error getting pattern", e);
 			}
 			return null;
 		}
 
 		@Override
 		public void format(Node note, SpannableStringBuilder sb, Matcher m,
 				String text, boolean selected) {
 			try { // Remote errors
 				FormatSpan[] spans = plugin.format(index, theme, note,
 						m.group(), selected);
 				if (null == spans) { // No spans
 					// Log.i(TAG, "No format");
 					return;
 				}
 				// Log.i(TAG, "format: " + text + ", " + spans.length);
 				for (FormatSpan span : spans) { // Have span
 					PlainTextFormatter.addSpan(sb, span.getText(),
 							span.getSpans());
 				}
 			} catch (Exception e) {
 				// Log.w(TAG, "Error formatting");
 			}
 		}
 
 	}
 
 	private List<NodeTextFormatter> formattersFromPlugin(Plugin plugin) {
 		List<NodeTextFormatter> result = new ArrayList<NodeTextFormatter>();
 		try { // Remote errors
 			int formatters = plugin.getFormatterCount();
 			// Log.i(TAG, "formattersFromPlugin: " + formatters + ", " +
 			// plugin);
 			for (int i = 0; i < formatters; i++) { // Create new Formatter
 				result.add(new PluginFormatter(plugin, i));
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Error creating plugin formatter", e);
 		}
 		return result;
 	}
 
 	public void resetRemoteRenders() {
 		remoteRenders.clear();
 		remoteRenderPlugins = controller
 				.getPlugins(PluginInfo.PLUGIN_CAN_RENDER);
 	}
 
 }
