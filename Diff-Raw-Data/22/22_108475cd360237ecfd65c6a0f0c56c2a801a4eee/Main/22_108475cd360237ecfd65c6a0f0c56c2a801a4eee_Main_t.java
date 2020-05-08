 // This file is part of Droopy.
 // Copyright (C) 2011  Benoit Sigoure.
 //
 // This program is free software: you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or (at your
 // option) any later version.  This program is distributed in the hope that it
 // will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 // of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 // General Public License for more details.  You should have received a copy
 // of the GNU Lesser General Public License along with this program.  If not,
 // see <http://www.gnu.org/licenses/>.
 package viewer;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DomEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONException;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.Selection;
 import com.google.gwt.visualization.client.VisualizationUtils;
 import com.google.gwt.visualization.client.events.SelectHandler;
 import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
 import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
 import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
 import com.google.gwt.visualization.client.visualizations.corechart.Options;
 import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
 
 import static viewer.Json.object;
 
 /**
  * Main class for the Droopy UI.
  */
 final class Main implements EntryPoint {
 
   private static final DateTimeFormat FULLDATE =
     DateTimeFormat.getFormat("yyyy/MM/dd-HH:mm:ss");
 
   /** Max number of results we'll fetch from ES.  */
   private static final short MAX_RESULTS = 200;
   private static final short DEFAULT_RESULTS = 20;
 
   private String server;
 
   private final VerticalPanel root = new VerticalPanel();
   private final InlineLabel status = new InlineLabel();
   private final HorizontalPanel charts = new HorizontalPanel();
   private final AlignedTree traces = new AlignedTree();
   private short nresults = DEFAULT_RESULTS;  // How many traces we want.
 
   private final DateTimeBox start_datebox = new DateTimeBox();
   private final DateTimeBox end_datebox = new DateTimeBox();
   private final TextBox esquery = new TextBox();
 
   private abstract class AjaxCallback implements RequestCallback/*AsyncCallback<JavaScriptObject>*/ {
     public void onError(final Request req, final Throwable e) {
       status.setText("AJAX query failed: " + e.getMessage());
     }
     public final void onResponseReceived(final Request request, final Response response) {
       final String text = response.getText();
       if (text.isEmpty()) {
         final int code = response.getStatusCode();
         final String errmsg;
         if (code == 0) {  // Happens when a cross-domain request fails to connect.
           errmsg = ("Failed to connect to " + server + ", check that the server"
                     + " is up and that you can connect to it.");
         } else {
           errmsg = ("Empty response from server: code=" + code
                     + " status=" + response.getStatusText());
         }
         onError(request, new RuntimeException(errmsg));
       } else {
         JSONValue value;
         try {
           value = JSONParser.parseStrict(text);
         } catch (JSONException e) {
           onError(request, e);
           return;
         }
         onSuccess(value);
       }
     }
     protected abstract void onSuccess(final JSONValue response);
   }
 
   /**
    * This is the entry point method.
    */
   public void onModuleLoad() {
     final class Start implements Runnable {
       public void run() {
         onModuleLoadReal();
       }
     }
     VisualizationUtils.loadVisualizationApi(new Start(), "corechart");
   }
 
   private void onModuleLoadReal() {
     server = getServer();
     if (server == null) {
       promptForServerUi();
       return;
     }
     status.setText("Checking server health...");
     root.add(status);
     root.add(charts);
     removeLoadingMessage();
     RootPanel.get().add(root);
     ajax("/droopy/_status", new AjaxCallback() {
       public void onSuccess(final JSONValue response) {
         final JSONObject resp = response.isObject();
         if (resp.containsKey("ok")
             && resp.get("ok").isBoolean().booleanValue()) {
           status.setText("Server is ready, "
                          + (resp.get("indices").isObject()
                             .get("droopy").isObject()
                             .get("docs").isObject()
                             .get("num_docs")) + " traces available."
                          + "  Loading ...");
           setupUi();
           setupHistory();
         } else if (resp.containsKey("error") && resp.containsKey("status")) {
           status.setText("Server health check failed: error "
                          + resp.get("status") + ": " + resp.get("error"));
         } else {
           status.setText("Incomprehensible response from server: " + resp);
         }
       }
     });
   }
 
   private static void removeLoadingMessage() {
     // Remove the static "Loading..." message.
     final Element loading = DOM.getElementById("loading");
     DOM.removeChild(DOM.getParent(loading), loading);
   }
 
   private static String getServer() {
     final String server = Window.Location.getParameter("srv");
     if (server == null || server.isEmpty()) {
       return null;
     }
     return "http://" + server;
   }
 
   private void promptForServerUi() {
     final VerticalPanel vbox = new VerticalPanel();
     vbox.add(new InlineLabel("I need to know the address of the ElasticSearch"
                              + " server where Droopy traces are stored."));
     final HorizontalPanel hbox = new HorizontalPanel();
     hbox.setSpacing(5);
     hbox.add(new InlineLabel("Address (host:port):"));
     final ValidatedTextBox textbox = new ValidatedTextBox();
     textbox.setValidationRegexp("^[-_.a-zA-Z0-9]+:[1-9][0-9]*$");
     final EventsHandler setsrv = new EventsHandler() {
       protected <H extends EventHandler> void onEvent(final DomEvent<H> event) {
         final String srv = textbox.getValue();
         if (srv == null || srv.isEmpty()) {
           return;
         }
         final StringBuilder url = new StringBuilder();
         url.append(Window.Location.getPath());
         String tmp = Window.Location.getQueryString();
         if (tmp != null && !tmp.isEmpty()) {
           url.append(tmp).append("&srv=");
         } else {
           url.append("?srv=");
         }
         url.append(srv);
         tmp = Window.Location.getHash();
         if (tmp != null && !tmp.isEmpty()) {
           url.append(tmp);
         }
         Window.Location.assign(url.toString());
       }
     };
     textbox.addBlurHandler(setsrv);
     textbox.addKeyPressHandler(setsrv);
     hbox.add(textbox);
     vbox.add(hbox);
     root.add(vbox);
     removeLoadingMessage();
     RootPanel.get().add(root);
   }
 
   private void ajax(final String resource, final AjaxCallback callback) {
     ajax(resource, null, callback);
   }
 
   private void ajax(final String resource, final String body,
                     final AjaxCallback callback) {
     final boolean has_body = body != null;
     final RequestBuilder builder =
       new RequestBuilder(has_body ? RequestBuilder.POST : RequestBuilder.GET,
                          server + resource);
     // Doesn't work on Chrome due to ES bug #828.
     //if (has_body) {
     //  builder.setHeader("Content-Type", "application/json");
     //}
     try {
       builder.sendRequest(body, callback);
     } catch (RequestException e) {
       status.setText("Failed to setup AJAX call to " + server + resource
                      + ": " + e);
     }
   }
 
   private void setupUi() {
     setupChangeHandlers();
     final HorizontalPanel hbox = new HorizontalPanel();
     hbox.setSpacing(5);
     hbox.add(new InlineLabel("From"));
     hbox.add(start_datebox);
     // If we're not trying to see anything already, look at some recent
     // traces by default.
     if (History.getToken().isEmpty()) {
       start_datebox.setValue(new Date(System.currentTimeMillis() - 600000), true);
     }
     hbox.add(new InlineLabel("To"));
     hbox.add(end_datebox);
     hbox.add(new InlineLabel("Query"));
     hbox.add(esquery);
     root.add(hbox);
     traces.setAnimationEnabled(true);
     root.add(traces);
   }
 
   private void setupChangeHandlers() {
     final EventsHandler refresh = new EventsHandler() {
       protected <H extends EventHandler> void onEvent(final DomEvent<H> event) {
         refresh();
       }
     };
 
     {
       final ValueChangeHandler<Date> vch = new ValueChangeHandler<Date>() {
         public void onValueChange(final ValueChangeEvent<Date> event) {
           refresh();
         }
       };
       TextBox tb = start_datebox.getTextBox();
       tb.addKeyPressHandler(refresh);
       start_datebox.addValueChangeHandler(vch);
       tb = end_datebox.getTextBox();
       tb.addKeyPressHandler(refresh);
       end_datebox.addValueChangeHandler(vch);
     }
     esquery.addKeyPressHandler(refresh);
   }
 
   private void setupHistory() {
     final ValueChangeHandler<String> handler = new ValueChangeHandler<String>() {
       public void onValueChange(final ValueChangeEvent<String> event) {
         final Map<String, List<String>> params = parseQueryString(event.getValue());
        setTextBox(start_datebox.getTextBox(), params.get("start"));
        setTextBox(end_datebox.getTextBox(), params.get("end"));
        setTextBox(esquery, params.get("q"));
         List<String> value;
         if ((value = params.get("results")) != null) {
           short n;
           try {
             n = Short.valueOf(value.get(0));
           } catch (NumberFormatException e) {
             n = -1;
           }
           if (0 < n && n < MAX_RESULTS) {
             nresults = n;
           }
         } else {
           nresults = DEFAULT_RESULTS;
         }
         loadTraces();
       }

      private void setTextBox(final TextBox box, final List<String> values) {
        if (values != null) {
          box.setValue(values.get(0));
        } else {
          box.setValue("");
        }
      }
     };
     History.addValueChangeHandler(handler);
     History.fireCurrentHistoryState();
   }
 
   private void refresh() {
     final Date start = start_datebox.getValue();
     if (start == null) {
       status.setText("Please specify a start time.");
       return;
     }
     final Date end = end_datebox.getValue();
     if (end != null && end.getTime() <= start.getTime()) {
         end_datebox.addStyleName("dateBoxFormatError");
         status.setText("End time must be after start time!");
         return;
     }
     final StringBuilder token = new StringBuilder();
     token.append("start=").append(FULLDATE.format(start));
     if (end != null) {
       token.append("&end=").append(FULLDATE.format(end));
     }
     if (!esquery.getValue().isEmpty()) {
       token.append("&q=").append(esquery.getValue());
     }
     History.newItem(token.toString());
   }
 
   private void loadTraces() {
     status.setText("Loading...");
     final Json request_ts = object()
       .add("from", toMillis(start_datebox));
     if (end_datebox.getValue() != null) {
       request_ts.add("to", toMillis(end_datebox));
     }
     final String json = object()
       .add("size", nresults)
       .add("sort", Json.array()
            .add("request_ts", object("order", "desc")))
       .add("query",
            object("filtered", object("query", getESQuery())
                   .add("filter",
                        object("numeric_range",
                               object("request_ts", request_ts)
                              )
                       )
                  )
           )
       .add("facets",
            object()
            .add("slowbe", object("terms", object("field", "prev_connect.host")))
            .add("betype", object("terms", object("field", "prev_connect.type")))
            //.add("lathisto", object("histogram",
            //                        object("field", "end_to_end").add("interval", 30)))
           )
       .toString();
     ajax("/droopy/summary/_search", json,
           new AjaxCallback() {
       public void onSuccess(final JSONValue response) {
         final ESResponse<Summary> resp = ESResponse.fromJson(response.isObject());
         status.setText("Found " + resp.hits().total() + " traces in "
                        + resp.took() + "ms");
 
         charts.clear();
         renderChart(resp.<ESResponse.TermFacet>facets("slowbe"),
                     "Slowest Backend Hosts", "host");
         renderChart(resp.<ESResponse.TermFacet>facets("betype"),
                     "Slowest Backend Types", "type");
         renderLatencyHistogram(resp.<ESResponse.HistoFacet>facets("lathisto"));
         renderTraces(resp.hits());
       }
     });
   }
 
   private Json getESQuery() {
     final String q = esquery.getValue();
     if (q.isEmpty()) {
       return object("match_all", object());
     }
     return object("query_string", object("query", q)
                   .add("default_operator", "AND"));
   }
 
   private void renderChart(final ESResponse.Facets<ESResponse.TermFacet> facets,
                            final String title, final String tag) {
     if (facets == null) {
       return;
     }
     final DataTable data = DataTable.create();
     data.addColumn(DataTable.ColumnType.STRING, "Backend");
     data.addColumn(DataTable.ColumnType.NUMBER, "Number of times slowest");
     final JsArray<ESResponse.TermFacet> terms = facets.terms();
     final int nterms = terms.length();
     data.addRows(nterms);
     for (int i = 0; i < nterms; i++) {
       final ESResponse.TermFacet facet = terms.get(i);
       final String backend = facet.term();
       if ("unknown".equals(backend)) {
         continue;
       }
       data.setValue(i, 0, backend);
       data.setValue(i, 1, facet.count());
     }
     final PieChart.PieOptions options = PieChart.createPieOptions();
     options.setWidth(400);
     options.setHeight(240);
     options.setTitle(title);
     final PieChart chart = new PieChart(data, options);
     final SearchOnSelectHandler handler = new SearchOnSelectHandler(chart, data, tag);
     chart.addSelectHandler(handler);
     charts.add(chart);
   }
 
   private final class SearchOnSelectHandler extends SelectHandler {
 
     /*
      * Just wanna say: the select handler API is ridiculously bad.
      * The SelectEvent we receive contains nothing, so we have to retain a
      * reference to the chart manually in the handler.  But that's not enough
      * because the chart doesn't have an API to access its data, so you also
      * need to manually retain a reference to the data in the chart.  The data
      * doesn't speak in Selection so you have to manually translate that into
      * a row/column request.  Sigh.
      */
 
     private final CoreChart chart;
     private final DataTable data;
     private final String tag;
 
     private SearchOnSelectHandler(final CoreChart chart,
                                   final DataTable data,
                                   final String tag) {
       this.chart = chart;
       this.data = data;
       this.tag = tag + ":";
     }
 
     public void onSelect(final SelectEvent event) {
       String qs = esquery.getValue();
       for (final Selection selection : JsArrayIterator.iter(chart.getSelections())) {
         if (!selection.isRow()) {
           continue;
         }
         qs += ' ' + tag + data.getValueString(selection.getRow(), 0);
       }
       esquery.setValue(qs.trim());
       refresh();
     }
 
   }
 
   private void renderLatencyHistogram(final ESResponse.Facets<ESResponse.HistoFacet> facets) {
     // Disabled because it triggers a JavaScript error in corechart.  See:
     // http://groups.google.com/group/gwt-google-apis/browse_thread/thread/332a644b2e7e66fc
 
     //if (facets == null) {
     //  return;
     //}
     //final DataTable data = DataTable.create();
     //data.addColumn(DataTable.ColumnType.NUMBER, "Latency");
     //data.addColumn(DataTable.ColumnType.NUMBER, "Number of hits");
     //final JsArray<ESResponse.HistoFacet> buckets = facets.terms();
     //final int nbuckets = buckets.length();
     //data.addRows(nbuckets);
     //for (int i = 0; i < nbuckets; i++) {
     //  final ESResponse.HistoFacet facet = buckets.get(i);
     //  data.setValue(i, 0, facet.key());
     //  data.setValue(i, 1, facet.count());
     //}
     //final Options options = ColumnChart.createOptions();
     //options.setWidth(400);
     //options.setHeight(240);
     //options.setTitle("Response Latency");
     //final AxisOptions axis = AxisOptions.create();
     //axis.setTitle("Latency");
     //options.setHAxisOptions(axis);
     //charts.add(new ColumnChart(data, options));
   }
 
   private void renderTraces(final ESResponse.Hits<Summary> summaries) {
     traces.clear();
     final HashSet<String> expanded = new HashSet(getHistoryTokens("trace"));
     for (final ESResponse.Hit<Summary> hit : summaries.iterator()) {
       final String id = hit.id();
       final Summary summary = hit.source();
       final TreeItem trace = new LazyTreeItem(summary.widget()) {
         protected void onFirstOpen() {
           expandTrace(this, id, summary);
           onOpen();
         }
         protected void onOpen() {
           appendHistoryToken("trace", id);
         }
         protected void onClose() {
           removeHistoryToken("trace", id);
         }
       };
       traces.addItem(trace);
       if (expanded.remove(id)) {  // If this trace ID should be expanded...
         trace.setState(true);     // then expand it now.
       }
     }
     if (nresults < MAX_RESULTS && summaries.total() > nresults) {
       final Button more = new Button("Load more traces", new ClickHandler() {
         public void onClick(final ClickEvent event) {
           nresults += 20;
           traces.getItem(traces.getItemCount() - 1).setText("Loading...");
           replaceHistoryTokens("results", Short.toString(nresults));
           loadTraces();
         }
       });
       traces.addItem(more);
     }
     traces.align();
     // IDs that haven't been expanded are no longer displayed,
     // so remove them from the URL.
     for (final String id : expanded) {
       removeHistoryToken("trace", id);
     }
   }
 
   private void expandTrace(final TreeItem parent, final String traceid,
                            final Summary summary) {
     ajax("/droopy/trace/" + traceid, new AjaxCallback() {
       public void onSuccess(final JSONValue response) {
         final ESResponse.Hit<Trace> hit = ESResponse.Hit.fromJson(response.isObject());
         parent.removeItems();
         parent.addItem(hit.source().widget(summary));
       }
     });
   }
 
   // ---------------- //
   // History helpers. //
   // ---------------- //
 
   private static void appendHistoryToken(final String key, final String value) {
     appendHistoryToken(key, value, false);
   }
 
   private static void appendHistoryToken(final String key, final String value,
                                          final boolean fire_event) {
     final String current = History.getToken();
     if (current.isEmpty()) {
       History.newItem(key + '=' + value, fire_event);
       return;
     }
     final Map<String, List<String>> params = parseQueryString(current);
     final List<String> values = params.get(key);
     if (values != null) {
       for (final String existing : values) {
         if (value.equals(existing)) {
           return;
         }
       }
     }
     History.newItem(current + '&' + key + '=' + value, fire_event);
   }
 
   private static void removeHistoryToken(final String key, final String value) {
     removeHistoryToken(key, value, false);
   }
 
   private static void removeHistoryToken(final String key, final String value,
                                          final boolean fire_event) {
     final String current = History.getToken();
     if (current.isEmpty()) {
       return;
     }
     final String search = key + '=' + value;
     if (current.startsWith(search)) {
       History.newItem(current.substring(search.length()));
       return;
     }
     final Map<String, List<String>> params = parseQueryString(current);
     final List<String> values = params.get(key);
     if (values != null) {
       for (final String existing : values) {
         if (value.equals(existing)) {
           final int index = current.indexOf('&' + search);
           if (index < 0) {  // Should never happen.  Be loud.
             Window.alert("WTF? Failed to find history token "
                          + search + " in URL");
           } else {
             History.newItem(current.substring(0, index)
                             + current.substring(index + search.length() + 1),
                             fire_event);
           }
           return;
         }
       }
     }
   }
 
   private static final List<String> getHistoryTokens(final String key) {
     final String token = History.getToken();
     if (token.isEmpty()) {
       return Collections.emptyList();
     }
     final Map<String, List<String>> params = parseQueryString(token);
     final List<String> values = params.get(key);
     // OK, this is an interesting peculiarity of the Java language.
     // I originally wrote the following line, but it doesn't compile:
     //return values == null ? Collections.emptyList() : values;
     // Because of this somewhat cryptic error:
     //   Type mismatch: cannot convert from
     //   List<capture#1-of ? extends Object> to List<String
     // This is because the type of the conditional expression is the "lub"
     // (lower upper bound) of the two operands.  So instead we have to help
     // the compiler and disambiguate the code with this C++-like syntax:
     return values == null ? Collections.<String>emptyList() : values;
   }
 
   private static final Map<String, List<String>> parseQueryString(final String querystring) {
     return QueryStringDecoder.getParameters(querystring);
   }
 
   private static void replaceHistoryTokens(final String key, final String value) {
     for (final String remove : getHistoryTokens(key)) {
       removeHistoryToken(key, remove);
     }
     appendHistoryToken(key, value);
   }
 
   // ------------- //
   // Misc helpers. //
   // ------------- //
 
   private static final long toMillis(final DateTimeBox box) {
     return box.getValue().getTime();
   }
 
 }
