 // Copyright 2012 Andy Boothe
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 package com.sigpwned.ldraw4j.io;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Pattern;
 
 import com.sigpwned.ldraw4j.io.x.MalformedFileLDRAWException;
 import com.sigpwned.ldraw4j.model.colour.ColourReference;
 import com.sigpwned.ldraw4j.model.colour.Material;
 import com.sigpwned.ldraw4j.model.colour.RGBA;
 import com.sigpwned.ldraw4j.model.colour.material.CustomMaterial;
 import com.sigpwned.ldraw4j.model.colour.material.GlitterMaterial;
 import com.sigpwned.ldraw4j.model.colour.material.SpeckleMaterial;
 import com.sigpwned.ldraw4j.model.colour.material.StockMaterial;
 import com.sigpwned.ldraw4j.model.colour.material.size.MinMaxSize;
 import com.sigpwned.ldraw4j.model.colour.material.size.SingleSize;
 import com.sigpwned.ldraw4j.model.colour.material.size.Size;
 import com.sigpwned.ldraw4j.model.colour.ref.CodeColourReference;
 import com.sigpwned.ldraw4j.model.colour.ref.RGBAColourReference;
 import com.sigpwned.ldraw4j.model.file.FileType;
 import com.sigpwned.ldraw4j.model.file.FileVersion;
 import com.sigpwned.ldraw4j.model.file.version.OriginalFileVersion;
 import com.sigpwned.ldraw4j.model.file.version.UpdateDateFileVersion;
 import com.sigpwned.ldraw4j.model.file.version.UpdateRevisionFileVersion;
 import com.sigpwned.ldraw4j.model.geometry.Matrix3f;
 import com.sigpwned.ldraw4j.model.geometry.Point3f;
 import com.sigpwned.ldraw4j.model.name.Name;
 import com.sigpwned.ldraw4j.model.name.RealName;
 import com.sigpwned.ldraw4j.model.name.UserName;
 import com.sigpwned.ldraw4j.model.winding.BFC;
 import com.sigpwned.ldraw4j.util.CollectionUtil;
 import com.sigpwned.ldraw4j.util.StringUtil;
 import com.sigpwned.ldraw4j.x.InternalLDRAWException;
 import com.sigpwned.ldraw4j.x.LDRAWException;
 import com.sigpwned.ldraw4j.x.LeakedLDRAWException;
 
 
 public class LDRAWReader {
 	private LDRAWReadHandler handler;
 	private BufferedReader lines;
 	private int lineno;
 	
 	public LDRAWReader(LDRAWReadHandler handler) {
 		this.handler = handler;
 	}
 	
 	public void read(Reader in) throws IOException, LDRAWException {
 		try {
 			try {
 				lines = new BufferedReader(in);
 				parse();
 			}
 			finally {
 				lines.close();
 				lines = null;
 			}
 		}
 		finally {
 			in.close();
 		}
 	}
 	
 	private static final Pattern LINE=Pattern.compile("^(\\d+)");
 	protected void parse() throws IOException, LDRAWException {
 		lineno = 0;
 		for(String line=lines.readLine();line!=null;line=lines.readLine()) {
 			line   = line.trim();
 			lineno = lineno+1;
 			if(line.equals("")) {
 				// Blank lines are ignored
 			}
 			else {
 				MatchResult m=StringUtil.match(line, LINE);
 				if(m == null)
 					throw new MalformedFileLDRAWException("Non-empty line does not begin with a number: "+line);
 				int code=Integer.parseInt(m.group(1));
 				String command=line.substring(m.end(), line.length()).trim();
 				if(code == 0)
 					meta(command);
 				else
 				if(code == 1)
 					subfile(command);
 				else
 				if(code == 2)
 					line(command);
 				else
 				if(code == 3)
 					triangle(command);
 				else
 				if(code == 4)
 					quadrilateral(command);
 				else
 				if(code == 5)
 					optionalLine(command);
 				else
 					throw new MalformedFileLDRAWException("Illegal code at beginning of line: "+line);
 			}
 		}
 	}
 	
 	private static final Pattern OPTIONALLINE=Pattern.compile("^(\\d+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s*$");
 	private void optionalLine(String line) throws LDRAWException {
 		MatchResult m=require(line, OPTIONALLINE, "Malformed optionalLine line: "+line);
 		
 		int colour=Integer.parseInt(m.group(1));
 		Point3f[] points=new Point3f[] {
 			p(m, 2), p(m, 5)
 		};
 		Point3f[] controlPoints=new Point3f[] {
 			p(m, 8), p(m, 11)
 		};
 		
 		handler.optionalLine(new CodeColourReference(colour), points, controlPoints);
 	}
 
 	private static final Pattern QUAD=Pattern.compile("^(\\d+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s*$");
 	private void quadrilateral(String line) throws LDRAWException {
 		MatchResult m=require(line, QUAD, "Malformed quadrilateral line: "+line);
 		
 		int colour=Integer.parseInt(m.group(1));
 		Point3f[] triangle=new Point3f[] {
 			p(m, 2), p(m, 5), p(m, 8), p(m, 11)
 		};
 		
 		handler.quadrilateral(new CodeColourReference(colour), triangle);
 	}
 
 	private static final Pattern TRIANGLE=Pattern.compile("^(\\d+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s*$");
 	private void triangle(String line) throws LDRAWException {
 		MatchResult m=require(line, TRIANGLE, "Malformed triangle line: "+line);
 		
 		int colour=Integer.parseInt(m.group(1));
 		Point3f[] triangle=new Point3f[] {
 			p(m, 2), p(m, 5), p(m, 8)
 		};
 		
 		handler.triangle(new CodeColourReference(colour), triangle);
 	}
 
 	private static final Pattern LINEPAT=Pattern.compile("^(\\d+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s+([+-]?[\\d.]+)\\s*$");
 	private void line(String line) throws LDRAWException {
 		MatchResult m=require(line, LINEPAT, "Malformed line line: "+line);
 		
 		int colour=Integer.parseInt(m.group(1));
 		
 		handler.line(new CodeColourReference(colour), new Point3f[] {
 			p(m, 2), p(m, 5)
 		});
 	}
 
 	private static final Pattern SUBFILE=Pattern.compile("^(\\d+)\\s+([+-]?[\\d.]+)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+([-+]?\\d+(?:.\\d*)?)\\s+(.*?)\\s*$");
 	private void subfile(String line) throws LDRAWException, IOException {
 		MatchResult m=require(line, SUBFILE, "Malformed subfile line: "+line);
 		
 		int colour=Integer.parseInt(m.group(1));
 		
 		Point3f location=p(m, 2);
 		
 		float[][] rotationCoords=new float[3][];
 		for(int i=0;i<3;i++) {
 			rotationCoords[i] = new float[3];
 			for(int j=0;j<3;j++)
 				rotationCoords[i][j] = Float.parseFloat(m.group(5+3*i+j));
 		}
 		Matrix3f rotation=new Matrix3f(rotationCoords);
 		
 		String file=m.group(14);
 		
 		handler.subfile(new CodeColourReference(colour), location, rotation, file);
 	}
 	
 	private static final DateFormat DATE=new SimpleDateFormat("yyyy-MM-dd");
 
 	private static final Pattern INT=Pattern.compile("^\\d+$");
 	private static final Pattern COMMENT=Pattern.compile("^//");
 	private static final Pattern COMMAND=Pattern.compile("^(!\\w+\\b|name:|author:|bfc\\b|clear\\b|pause\\b|print\\b|save\\b|step\\b|write\\b)", Pattern.CASE_INSENSITIVE);
 	private static final Pattern AUTHOR_ARGS=Pattern.compile("^([^\\[]+?)?\\s*(?:\\[([^\\]]+)\\])?$");
 	private static final Pattern LDRAW_ORG_ARGS=Pattern.compile("^(part|subpart|primitive|48_primitive|shortcut|configuration)\\s+(\\S.*?\\s+)?(original|update\\s+(\\d+)-(\\d+)(?:-(\\d+))?)$", Pattern.CASE_INSENSITIVE);
 	private static final Pattern LICENSE_REDISTRIBUTABLE=Pattern.compile("(?!=not)\\s*redistributable", Pattern.CASE_INSENSITIVE);
 	private static final Pattern HISTORY_ARGS=Pattern.compile("^(\\d+-\\d+-\\d+)\\s+(\\[[^\\]]+\\]|\\{[^\\}]+\\})\\s+(.*?)\\s*$", Pattern.CASE_INSENSITIVE);
 	private static final Pattern COLOUR_ARGS=Pattern.compile("^(.*?)\\s+CODE\\s+(\\d+)\\s+VALUE\\s+((?:0[xX]|#)[0-9A-Fa-f]{6})\\s+EDGE\\s+((?:0[xX]|#)[0-9A-Fa-f]{6}|\\d+)(?:\\s+ALPHA\\s+(\\d+))?(?:\\s+LUMINANCE\\s+(\\d+))?(?:\\s+(CHROME|PEARLESCENT|RUBBER|MATTE_METALLIC|METALLIC|METAL|MATERIAL\\s+.*?))?\\s*$");
 	protected void meta(String line) throws LDRAWException {
 		MatchResult m=StringUtil.match(line, COMMAND);
 		if(m != null) {
 			String command=m.group().toLowerCase();
 			String arguments=line.substring(m.end(), line.length()).trim();
 			if(command.equals("!name") || command.equals("name:"))
 				handler.name(arguments);
 			else
 			if(command.equals("!author") || command.equals("author:")) {
 				m = require(arguments, AUTHOR_ARGS, "Malformed author line: "+arguments);
 				RealName realName=new RealName(m.group(1));
 				UserName userName=null;
 				if(m.group(2) != null)
 					userName = new UserName(m.group(2));
 				handler.author(realName, userName);
 			} else
 			if(command.equals("!ldraw_org")) {
 				m = require(arguments, LDRAW_ORG_ARGS, "Malformed LDRAW_ORG line: "+arguments);
 				
 				String partTypeName=m.group(1).toLowerCase();
 				if(partTypeName.equals("48_primitive"))
 					partTypeName = "primitive48";
 				FileType partType=valueOf(FileType.class, partTypeName.toUpperCase());
 
 				String qualifiers=m.group(2);
 
 				FileVersion partVersion;
 				String partVersionText=m.group(3).toLowerCase();
 				if(partVersionText.equals("original"))
 					partVersion = OriginalFileVersion.getInstance();
 				else
 				if(m.group(6) != null) {
 					String dateline=m.group(4)+"-"+m.group(5)+"-"+m.group(6);
 					partVersion = new UpdateDateFileVersion(d(dateline, DATE, "Invalid date in LDRAW_ORG line: "+dateline));
 				}
 				else
 					partVersion = new UpdateRevisionFileVersion(Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)));
 				
 				handler.ldraworg(partType, qualifiers, partVersion);
 			} else
 			if(command.equals("!bfc") || command.equals("bfc")) {
 				String bfcName=CollectionUtil.join(StringUtil.toUpperCase(StringUtil.words(arguments)), "_");
 				BFC bfc=valueOf(BFC.class, bfcName);
 				handler.bfc(bfc);
 			} else
 			if(command.equals("!history")) {
 				m = require(arguments, HISTORY_ARGS, "Malformed HISTORY line: "+arguments);
 				
 				Date date=d(m.group(1), DATE, "Bad date in HISTORY line: "+m.group(1));
 				Name name;
 				if(m.group(2).startsWith("{"))
 					name = new RealName(m.group(2).substring(1, m.group(2).length()-1));
 				else
 				if(m.group(2).startsWith("["))
 					name = new UserName(m.group(2).substring(1, m.group(2).length()-1));
 				else
 					throw new InternalLDRAWException("Unhandled case for name: "+m.group(2));
 				String message=m.group(3);
 				
 				handler.history(date, name, message);
 			} else
 			if(command.equals("!colour")) {
 				// Man, this command is a thing. Have a look at this page for details:
 				// http://www.ldraw.org/article/547.html
 				
 				m = require(arguments, COLOUR_ARGS, "Malformed COLOUR line: "+arguments);
 				
 				String name=m.group(1);
 				int code=Integer.parseInt(m.group(2));
 				
 				ColourReference edge;
 				if(StringUtil.match(m.group(4), INT) != null)
 					edge = new CodeColourReference(Integer.parseInt(m.group(4)));
 				else
 					edge = new RGBAColourReference(rgba(m.group(4), null));
 				
 				Integer alpha;
 				if(m.group(5) != null)
 					alpha = Integer.parseInt(m.group(5));
 				else
 					alpha = null;
 				
 				RGBA value=rgba(m.group(3), alpha);
 				
 				Integer luminance;
 				if(m.group(6) != null)
 					luminance = Integer.parseInt(m.group(6));
 				else
 					luminance = null;
 				
 				Material material;
 				if(m.group(7) != null)
 					material = material(m.group(7));
 				else
 					material = null;
 				
 				handler.colour(name, code, value, edge, luminance, material);
 			} else
 			if(command.equals("!license"))
 				handler.license(arguments, StringUtil.match(arguments, LICENSE_REDISTRIBUTABLE)!=null);
 			else
 			if(command.equals("!category"))
 				handler.category(arguments);
 			else
 			if(command.equals("!cmdline"))
 				handler.commandLine(arguments);
 			else
 			if(command.equals("!keywords"))
 				handler.keywords(StringUtil.trim(StringUtil.split(arguments, StringUtil.COMMA)));
 			else
 			if(command.equals("!help"))
 				handler.help(arguments);
 			else
 			if(command.equals("clear") || command.equals("!clear"))
 				handler.clear();
 			else
 			if(command.equals("pause") || command.equals("!pause"))
 				handler.pause();
 			else
 			if(command.equals("print") || command.equals("!print"))
 				handler.print(arguments);
 			else
 			if(command.equals("save") || command.equals("!save"))
 				handler.save();
 			else
 			if(command.equals("step") || command.equals("!step"))
 				handler.step();
 			else
 			if(command.equals("write") || command.equals("!write"))
 				handler.write(arguments);
 			else
 				handler.meta(line);
 		} else
 		if(lineno == 1) {
 			// This is an explicit property of the header. First line with a
 			// 0 is always the part description.
 			handler.partDescription(line);
 		} else
 		if(line.equals("")) {
 			// This is typically the last line in a file. NBD.
 		}
 		else {
			if(StringUtil.match(line, COMMENT) == null)
				System.err.println("WARNING: Line not marked explicitly as a comment being interpreted as comment: "+line);
 			handler.comment(line);
 		}
 	}
 	
 	private static <T extends Enum<T>> T valueOf(Class<T> klass, String name) throws LDRAWException {
 		T result;
 		try {
 			result = klass.cast(klass.getMethod("valueOf", String.class).invoke(null, name));
 		}
 		catch(IllegalArgumentException e) {
 			throw new MalformedFileLDRAWException("Not a valid "+klass.getSimpleName()+" value: "+name);
 		}
 		catch(Exception e) {
 			throw new LeakedLDRAWException(e);
 		}
 		return result;
 	}
 	
 	private static Point3f p(MatchResult m, int start) {
 		float[] coords=new float[3];
 		for(int i=0;i<3;i++)
 			coords[i] = Float.parseFloat(m.group(start+i));
 		return new Point3f(coords);
 	}
 	
 	private static Date d(String value, DateFormat format, String failure) throws LDRAWException {
 		Date result;
 		try {
 			result = format.parse(value);
 		}
 		catch(ParseException e) {
 			throw new MalformedFileLDRAWException(failure);
 		}
 		return result;
 	}
 	
 	private static MatchResult require(String s, Pattern p, String failure) throws LDRAWException {
 		MatchResult result=StringUtil.match(s, p);
 		if(result == null)
 			throw new MalformedFileLDRAWException(failure);
 		return result;
 	}
 	
 	private static RGBA rgba(String rgb0, Integer a) throws LDRAWException {
 		String rgb=rgb0;
 		if(rgb.startsWith("0x") || rgb.startsWith("0X"))
 			rgb = rgb.substring(2, rgb.length());
 		if(rgb.startsWith("#"))
 			rgb = rgb.substring(1, rgb.length());
 		if(rgb.length() != 6)
 			throw new MalformedFileLDRAWException("Invalid RGB value: "+rgb0);
 		
 		return RGBA.rgba(
 			Integer.parseInt(rgb.substring(0, 2), 16),
 			Integer.parseInt(rgb.substring(2, 4), 16),
 			Integer.parseInt(rgb.substring(4, 6), 16),
 			a!=null ? a.intValue() & 0xFF : 255
 		);
 	}
 	
 	private static final Pattern MATERIAL=Pattern.compile("^(CHROME|PEARLESCENT|RUBBER|MATTE_METALLIC|METAL|MATERIAL)(\\s+.*?)?\\s*$");
 	private static final Pattern GLITTER=Pattern.compile("^GLITTER\\s+VALUE\\s+((?:0x|#)[0-9A-Fa-f]{6})\\s+(?:ALPHA\\s+(\\d+)\\s+)?(?:LUMINANCE\\s+(\\d+)\\s+)?FRACTION\\s+(\\d+\\.\\d*)\\s+VFRACTION\\s+(\\d+\\.\\d*)\\s+(SIZE.*?|MINSIZE.*?)\\s*$");
 	private static final Pattern SPECKLE=Pattern.compile("^SPECKLE\\s+VALUE\\s+((?:0x|#)[0-9A-Fa-f]{6})\\s+(?:ALPHA\\s+(\\d+)\\s+)?(?:LUMINANCE\\s+(\\d+)\\s+)?FRACTION\\s+(\\d+\\.\\d*)\\s+(SIZE.*?|MINSIZE.*?)\\s*$");
 	private static Material material(String material) throws LDRAWException {
 		MatchResult m=require(material, MATERIAL, "Invalid material syntax: "+material);
 		
 		Material result;
 		try {
 			result = StockMaterial.valueOf(m.group(1));
 		}
 		catch(IllegalArgumentException e) {
 			result = null;
 		}
 		
 		if(result == null) {
 			String type=m.group(1);
 			if(!type.equals("MATERIAL"))
 				throw new MalformedFileLDRAWException("Invalid stock material: "+type);
 			
 			String arguments=m.group(2);
 			if(arguments == null)
 				throw new MalformedFileLDRAWException("MATERIALS clause requires arguments: "+material);
 			
 			if((m = StringUtil.match(arguments, GLITTER)) != null) {
 				Integer alpha;
 				if(m.group(2) != null)
 					alpha = Integer.parseInt(m.group(2));
 				else
 					alpha = null;
 				RGBA value=rgba(m.group(1), alpha);
 				
 				Integer luminance;
 				if(m.group(3) != null)
 					luminance = Integer.parseInt(m.group(6));
 				else
 					luminance = null;
 				
 				float fraction=Float.parseFloat(m.group(4));
 				float vfraction=Float.parseFloat(m.group(5));
 				
 				Size size=size(m.group(6));
 				
 				result = new GlitterMaterial(value, luminance, fraction, vfraction, size);
 			} else
 			if((m = StringUtil.match(arguments, SPECKLE)) != null) {
 				Integer alpha;
 				if(m.group(2) != null)
 					alpha = Integer.parseInt(m.group(2));
 				else
 					alpha = null;
 				RGBA value=rgba(m.group(1), alpha);
 				
 				Integer luminance;
 				if(m.group(3) != null)
 					luminance = Integer.parseInt(m.group(6));
 				else
 					luminance = null;
 				
 				float fraction=Float.parseFloat(m.group(4));
 				
 				Size size=size(m.group(5));
 				
 				result = new SpeckleMaterial(value, luminance, fraction, size);
 			}
 			else
 				result = new CustomMaterial(arguments);
 		}
 		
 		return result;
 	}
 	
 	private static final Pattern SIZE=Pattern.compile("^SIZE\\s+(\\d+)|MINSIZE\\s+(\\d+)\\s+MAXSIZE\\s+(\\d+)\\s*$");
 	private static Size size(String size) throws LDRAWException {
 		MatchResult m=require(size, SIZE, "Invalid material size: "+size);
 		
 		Size result;
 		if(m.group(1) != null)
 			result = new SingleSize(Integer.parseInt(m.group(1)));
 		else
 		if(m.group(2) != null)
 			result = new MinMaxSize(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
 		else
 			throw new InternalLDRAWException("Did not parse size: "+size);
 		
 		return result;
 	}
 	
 	public static void main(String[] args) {
 		try {
 			final Writer out=new OutputStreamWriter(System.out);
 			LDRAWReader reader=new LDRAWReader(new LDRAWReadHandler() {
 				private void println(String message) throws LDRAWException {
 					try {
 						out.write(message); out.write('\n'); out.flush();
 					}
 					catch(IOException e) {
 						throw new LeakedLDRAWException(e);
 					}
 				}
 				public void partDescription(String description) throws LDRAWException {
 					println("0 "+description);
 				}
 				public void name(String filename) throws LDRAWException {
 					println("0 Name: "+filename);
 				}
 				public void author(RealName realName, UserName userName) throws LDRAWException {
 					String line="0 Author: "+realName.getName();
 					if(userName != null)
 						line = line+" ["+userName.getName()+"]";
 					println(line);
 				}
 				public void ldraworg(FileType partType, String qualifiers, FileVersion version) throws LDRAWException {
 					String line="0 !LDRAW_ORG "+partType.toString()+" ";
 					if(qualifiers != null)
 						line = line+qualifiers+" ";
 					line = line+version;
 					println(line);
 				}
 				public void license(String licenseStatement, boolean redistributable) throws LDRAWException {
 					println("0 !LICENSE "+licenseStatement);
 				}
 				public void bfc(BFC bfc) throws LDRAWException {
 					println("0 BFC "+bfc.toString());
 				}
 				private final DateFormat DATE=new SimpleDateFormat("yyyy-MM-dd");
 				public void history(Date date, Name name, String message) throws LDRAWException {
 					println("0 !HISTORY "+DATE.format(date)+" "+name+" "+message);
 				}
 				public void category(String category) throws LDRAWException {
 					println("0 !CATEGORY "+category);
 				}
 				public void commandLine(String commandLine) throws LDRAWException {
 					println("0 !CMDLINE "+commandLine);
 				}
 				public void keywords(List<String> keywords) throws LDRAWException {
 					println("0 !KEYWORDS "+CollectionUtil.join(keywords, ", "));
 				}
 				public void help(String message) throws LDRAWException {
 					println("0 !HELP "+message);
 				}
 				public void clear() throws LDRAWException {
 					println("0 !CLEAR");
 				}
 				public void colour(String name, int code, RGBA value, ColourReference edge, Integer luminance, Material material) throws LDRAWException {
 					String line="0 !COLOUR "+name+" CODE "+code+" VALUE "+value.getRGBString()+" EDGE "+edge;
 					if(value.isAlphaDefined())
 						line = line+" ALPHA "+value.getAlpha();
 					if(luminance != null)
 						line = line+" LUMINANCE "+luminance;
 					if(material != null)
 						line = line+" "+material;
 					println("0 !COLOUR "+line);
 				}
 				public void pause() throws LDRAWException {
 				}
 				public void print(String message) throws LDRAWException {
 				}
 				public void save() throws LDRAWException {
 				}
 				public void step() throws LDRAWException {
 					println("0 STEP");
 				}
 				public void write(String message) throws LDRAWException {
 				}
 				public void meta(String line) throws LDRAWException {
 				}
 				public void subfile(ColourReference colour, Point3f location, Matrix3f rotation, String file) throws LDRAWException {
 					println("1 "+colour+" "+location+" "+rotation+" "+file);
 				}
 				public void line(ColourReference colour, Point3f[] line) throws LDRAWException {
 					String l="2 "+colour;
 					for(Point3f p : line)
 						l = l+" "+p;
 					println(l);
 				}
 				public void triangle(ColourReference colour, Point3f[] triangle) throws LDRAWException {
 					String line="3 "+colour;
 					for(Point3f p : triangle)
 						line = line+" "+p;
 					println(line);
 				}
 				public void quadrilateral(ColourReference colour, Point3f[] quad) throws LDRAWException {
 					String line="4 "+colour;
 					for(Point3f p : quad)
 						line = line+" "+p;
 					println(line);
 				}
 				public void optionalLine(ColourReference colour, Point3f[] line, Point3f[] controlPoints) throws LDRAWException {
 					String l="5 "+colour;
 					for(Point3f p : line)
 						l = l+" "+p;
 					for(Point3f p : controlPoints)
 						l = l+" "+p;
 					println(l);
 				}
 				public void comment(String comment) throws LDRAWException {
 				}
 			});
 //			reader.read(new FileReader("/Users/aboothe/Documents/workspaces/lego/legocad/LDRAW/ldraw/parts/154.dat"));
 			reader.read(new FileReader("/Users/aboothe/Documents/workspaces/lego/legocad/LDRAW/ldraw/parts/85863.dat"));
 //			reader.read(new FileReader("/Users/aboothe/Documents/workspaces/lego/legocad/LDRAW/ldraw/LDConfig.ldr"));
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
