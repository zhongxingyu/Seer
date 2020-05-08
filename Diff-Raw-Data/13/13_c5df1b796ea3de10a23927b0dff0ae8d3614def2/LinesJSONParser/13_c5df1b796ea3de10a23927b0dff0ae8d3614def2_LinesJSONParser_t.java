 package com.karhatsu.suosikkipysakit.datasource.parsers;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.karhatsu.suosikkipysakit.datasource.DataNotFoundException;
 import com.karhatsu.suosikkipysakit.domain.Line;
 import com.karhatsu.suosikkipysakit.domain.Stop;
 
 public class LinesJSONParser implements JSONParser<ArrayList<Line>> {
 
 	@Override
 	public ArrayList<Line> parse(String json) throws DataNotFoundException,
 			JSONException {
 		if (json.equals("")) {
 			throw new DataNotFoundException();
 		}
 		ArrayList<Line> lines = new ArrayList<Line>();
 		JSONArray jsonLines = new JSONArray(json);
 		for (int i = 0; i < jsonLines.length(); i++) {
 			JSONObject jsonLine = jsonLines.getJSONObject(i);
 			lines.add(parseLine(jsonLine));
 		}
 		return lines;
 	}
 
 	private Line parseLine(JSONObject jsonLine) throws JSONException {
 		String lineCode = jsonLine.getString("code_short");
 		String name = jsonLine.getString("name");
 		String lineStart = jsonLine.getString("line_start");
 		String lineEnd = jsonLine.getString("line_end");
 		Line line = new Line(lineCode, name, lineStart, lineEnd);
 		line.setStops(parseStops(jsonLine));
 		return line;
 	}
 
 	private ArrayList<Stop> parseStops(JSONObject jsonLine)
 			throws JSONException {
 		ArrayList<Stop> stops = new ArrayList<Stop>();
 		JSONArray jsonStops = jsonLine.getJSONArray("line_stops");
 		for (int i = 0; i < jsonStops.length(); i++) {
 			JSONObject jsonStop = jsonStops.getJSONObject(i);
 			String code = jsonStop.getString("codeShort");
			if (code.equals("")) {
				code = jsonStop.getString("code");
			}
 			String name = jsonStop.getString("name");
 			String coordinates = jsonStop.getString("coords");
 			stops.add(new Stop(code, name, coordinates));
 		}
 		return stops;
 	}
 
 }
