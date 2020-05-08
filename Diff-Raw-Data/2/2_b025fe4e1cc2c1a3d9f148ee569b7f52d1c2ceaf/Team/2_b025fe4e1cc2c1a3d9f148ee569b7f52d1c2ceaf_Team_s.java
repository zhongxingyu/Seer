 package net.djmacgyver.bgt.user;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.map.HSVManipulationMatrix;
 import android.content.Context;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.drawable.Drawable;
 
 public class Team {
 	private static Team[] teams = new Team[7];
 	private static Team anonymousTeam;
 	private static float[][] teamColors = {
 		// Team 1 gets a nice red
 		{15, 2, 1},
 		// Team 2 gets the original png color, kind of orange
 		{0,    1,    1},
 		// Team 3 gets deep purple
 		{109,  1.4f,   .5f},
 		// Team 4 is bright blue
 		{165,  1,    1.1f},
 		// Team 5 is bright green
 		{-106, 1,    1.1f},
 		// Team 6 wishes to be blue
 		{170, 1.6f, .6f},
 		// Team 7 is yellow
 		{-37,   2.2f,    1}
 	};
 	
 	private String name;
 	private Drawable pin;
 	
 	protected Team(String name, Drawable d) {
 		this.name = name;
 		this.pin = d;
 	}
 	
 	public static Team getTeam(String name, Context context) {
 		Pattern p = Pattern.compile("([0-9]+)");
 		Matcher m = p.matcher(name);
 		if (!m.find()) return getAnonymousTeam(context);
 		int teamId = Integer.valueOf(m.group(0));
 		if (teamId > teams.length) return getAnonymousTeam(context);
		if (teams[teamId -1] != null) return teams[teamId];
 		
 		Drawable d = context.getResources().getDrawable(R.drawable.pin_common).mutate();
 		float[] colors = teamColors[teamId - 1];
     	d.setColorFilter(new ColorMatrixColorFilter(new HSVManipulationMatrix(colors[0], colors[1], colors[2])));
     	d.setBounds(d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
     	
 		Team team = new Team(name, d);
 		teams[teamId - 1] = team;
 		return team;
 	}
 	
 	public static Team getAnonymousTeam(Context context)
 	{
 		if (anonymousTeam == null) {
 			Drawable d = context.getResources().getDrawable(R.drawable.pin).mutate();
 	    	d.setBounds(d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
 	    	d.setAlpha(127);
 			anonymousTeam = new Team("anonymous", d);
 		}
 		return anonymousTeam;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Drawable getPin() {
 		return pin;
 	}
 }
