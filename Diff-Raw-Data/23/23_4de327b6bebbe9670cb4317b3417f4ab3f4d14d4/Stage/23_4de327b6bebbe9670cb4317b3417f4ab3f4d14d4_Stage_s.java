 import java.io.*;
 import java.util.*;
 import java.awt.*;
 import javax.imageio.ImageIO;
 import java.lang.Math.*;
 
 public class Stage{
 	private String stageName;
 	private Image[] images;
 	private Instruction[][] instructions;
 	private Map<String,Integer> anchors;
 	private int enemyTotal;
 	private int bulletTotal;
 	
 	int PC = 0;
 	
 	public Stage(String stageName){
 		this.stageName = stageName;
 		String path = "map/" + stageName + "/";
 		InstructionCompiler compiler = new InstructionCompiler(path);
 		instructions = compiler.getInstructionTable();
 		images       = compiler.getImageTable();
 		enemyTotal   = compiler.getEnemyTotal();
 		bulletTotal  = compiler.getBulletTotal();
 	}
 	public Instruction[] get(){
 		if(PC < instructions.length){
 			return instructions[PC++];
 		}
 		else{
 			return null;
 		}
 	}
 	public Instruction[] get(long time){
 		if(time < instructions.length){
 			return instructions[(new Long(time)).intValue()];
 		}
 		else{
 			return null;
 		}
 	}
 	public void jump(String anchor){
 		Integer target = anchors.get(anchor);
 		if(target != null){
 			PC = target;
 		}
 	}
 	public void restart(){
 		PC = 0;
 	}
 	public Image getImage(int imageId){
 		if(imageId < images.length)
 			return images[imageId];
 		else
 			return null;
 	}
 	public int getEnemyTotal(){
 		return enemyTotal;
 	}
 	public int getBulletTotal(){
 		return bulletTotal;
 	}
 }
 
 
 class InstructionCompiler{
 	private ArrayList<ArrayList<Instruction>> instructionTable;
 	private ArrayList<Image> images;
 	private String path;
 	private BufferedReader reader;
 	private Root root;
 	
 	private VariableTable variables;
 	private int enemyID = 1;
 	private int bulletID = 1;
 	private int wave = 1;
 	
 	
 	public InstructionCompiler(String path){
 		this.path = path;
 		File map;
 		try{
 			map = new File(path + "map");
 			this.reader = new BufferedReader(new FileReader(map));
 		}
 		catch(FileNotFoundException e){
 			System.err.println("File is not found");
 		}
 		
 		loadDefaultImage();
 		analyze();
 		compile();
 	}
 	
 	public Instruction[][] getInstructionTable(){
 		Instruction[][] out;
 		Object[] array = instructionTable.toArray();
 		int length = array.length;
 		out = new Instruction[length][];
 		for(int i=0;i<length;i++){
 			ArrayList<Instruction> arr = (ArrayList<Instruction>)array[i];
 			if(array[i] != null)
 				out[i] = arr.toArray(new Instruction[arr.size()]);
 		}
 		
 		
 		/* Dump */
 		if(Nightmare.debug){
 			for(int i=0;i<length;i++){
 				Instruction[] arr = out[i];
 				if(arr != null){
 					for(Instruction inst:arr){
 						System.out.println(i+"\t:\t"+inst.toString());
 					}
 				}
 			}
 		}
 		
 		
 		return out;
 	}
 	
 	public Image[] getImageTable(){
 		Image[] out =  images.toArray(new Image[images.size()]);
 		return out;
 	}
 	public int getEnemyTotal(){
 		return enemyID;
 	}
 	public int getBulletTotal(){
 		return bulletID;
 	}
 	
 	
 	private void loadDefaultImage(){
 		images = new ArrayList<Image>();
 		try{
 			File imageIndex;
 			imageIndex = new File("image/image");
 			BufferedReader reader = new BufferedReader(new FileReader(imageIndex));
 			
 			String line;
 			while((line = reader.readLine()) != null && !(line.equals(""))){
 				Scanner tokens = new Scanner(line);
 				int imageID = tokens.nextInt();
 				String imageName = tokens.next();
 				Image image;
 				try{
 					image = ImageIO.read(new File("image/"+imageName));
 					int size = images.size();
 					for(int i=size;i<imageID;i++){
 						images.add(null);
 					}
 				images.add(imageID,image);
 				}
 				catch(IOException e){
 					System.err.println("Image '"+imageName+"' can't open");
 				}
 			}
 		}
 		catch(IOException e){
 			System.err.println(e.getMessage());
 		}
 	}
 	private void analyze(){
 		root = new Root();
 	}
 	private void compile(){
 		instructionTable = new ArrayList<ArrayList<Instruction>>();
 		variables = new VariableTable();
 		root.calc();
 	}
 	private void addInstruction(int time,Instruction instruction){
 		int size = instructionTable.size();
 		for(int i=size;i<=time;i++)
 			instructionTable.add(null);
 		ArrayList<Instruction> instructions;
 		if((instructions = instructionTable.get(time)) == null){
 			instructions = new ArrayList<Instruction>();
 			instructionTable.set(time,instructions);
 		}
 		instructions.add(instruction);
 	}
 	
 	
 	private interface Element{
 		public void calc();
 	}
 	
 
 
 ////////////////////////////////////////////////////////////////////////////////
 
 
 	private class Root implements Element{
 		private ArrayList<Element> elements = new ArrayList<Element>();
 		public Root(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("PATH"))      {elements.add(new Path(line));}
 					//else if(statement.equals("ROLE")) {elements.add(new Role(line));}
 					//else if(statement.equals("IMAGE")){elements.add(new Image(line));}
 					//else if(statement.equals("BOSS")) {elements.add(new Boss(line));}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		public void calc(){
 			for(Element e:elements){
 				e.calc();
 			}
 		}
 	}
 	
 
 
 ////////////////////////////////////////////////////////////////////////////////
 	
 	
 	private class Path implements Element{
 		private ArrayList<Element> elements = new ArrayList<Element>();
 		private String attribute;
 		public Path(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("ENEMY"))   {elements.add(new Enemy(line));}
 					if(statement.equals("GROUP"))   {elements.add(new Group(line));}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		public void calc(){
 			String time = attr(attribute,1);
 			variables.set("time",time);
 			for(Element e:elements){
 				e.calc();
 			}
 		}
 	}
 	
 
 
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Group implements Element{
 		private ArrayList<Element> elements = new ArrayList<Element>();
 		private String attribute;
 		public Group(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("ENEMY"))   {elements.add(new Enemy(line));}
 					if(statement.equals("GROUP"))   {elements.add(new Group(line));}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		
 		
 		
 		public void calc(){
 			int baseTime = parseInt(variables.get("time"));
 			int plusTime = parseInt(attr(attribute,1));
 			int times    = parseInt(attr(attribute,2));
 			int interval = parseInt(attr(attribute,3));
 			for(Element e:elements){
 				for(int i=0;i<times;i++){
 					variables.push();
 					variables.set("time",Integer.toString(baseTime + plusTime + i * interval));
 					e.calc();
 					variables.pop();
 				}
 			}
 		}
 	}
 	
 	
 ////////////////////////////////////////////////////////////////////////////////	
 
 
 	private class Enemy implements Element{
 		private Type type;
 		private Position position;
 		private Move move;
 		private Shoot shoot;
 		
 		private String attribute;
 		private String[] arguments = new String[7];
 		public Enemy(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("TYPE"))    {type     = new Type(line);}
 					if(statement.equals("POSITION")){position = new Position(line);}
 					if(statement.equals("MOVE"))    {move     = new Move(line);}
 					if(statement.equals("SHOOT"))   {shoot    = new Shoot(line);}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		
 		
 		
 		public void calc(){
 			int baseTime = parseInt(variables.get("time"));
 			int plusTime = parseInt(attr(attribute,1));
 			int time = baseTime + plusTime;
 			
 			/* inst_type */
 			arguments[0] = "enemy";
 			/* enemy_ID */
 			arguments[1] = Integer.toString(enemyID);
 			type.calc();
 			position.calc();
 			Instruction instruction = new Instruction(arguments);
 			addInstruction(time,instruction);
 			
 			variables.push();
 			variables.set("time",Integer.toString(time));
 			variables.set("target_ID",Integer.toString(enemyID));
 			variables.set("target_type","enemy");
 			move.calc();
 			shoot.calc();
 			variables.pop();
 			
 			enemyID += 1;
 		}
 		
 		
 		
 		private class Type{
 			private String attribute;
 			public Type(String attribute){
 				this.attribute = attribute;
 			}
 			public void calc(){
 				/* enemy_type*/
 				String type = attr(attribute,1);
 				arguments[2] = type;
 				if(type.equals("custom")){
 					/* image_ID */
 					arguments[4] = attr(attribute,2);
 					/* radius */
 					arguments[5] = attr(attribute,3);
 					/* HP */
 					arguments[6] = attr(attribute,4);
 				}
 			}
 		}
 		private class Position{
 			private String attribute;
 			public Position(String attribute){
 				this.attribute = attribute;
 			}
 			public void calc(){
 				arguments[3] = attr(attribute,1);
 			}
 		}
 	}
 	
 	
 
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Move implements Element{
 		private ArrayList<Element> elements = new ArrayList<Element>();
 		private String attribute;
 		public Move(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("ROUTE"))   {elements.add(new Route(line));}
 					if(statement.equals("SHAPE"))   {elements.add(new Shape(line));}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		public void calc(){
 			for(Element e:elements){
 				e.calc();
 			}
 		}
 	}
 	
 	
 
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Shoot implements Element{
 		private ArrayList<Element> elements = new ArrayList<Element>();
 		private String attribute;
 		public Shoot(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("BULLET"))  {elements.add(new Bullet(line));}
 					if(statement.equals("CLIP"))    {elements.add(new Clip(line));}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		public void calc(){
 			for(Element e:elements){
 				e.calc();
 			}
 		}
 	}
 	
 	
 
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Clip implements Element{
 		private ArrayList<Element> elements = new ArrayList<Element>();
 		private String attribute;
 		public Clip(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("BULLET"))  {elements.add(new Bullet(line));}
 					if(statement.equals("CLIP"))    {elements.add(new Clip(line));}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		
 		
 		
 		public void calc(){
 			int baseTime = parseInt(variables.get("time"));
 			int plusTime = parseInt(attr(attribute,1));
 			int times    = parseInt(attr(attribute,2));
 			int interval = parseInt(attr(attribute,3));
 			for(Element e:elements){
 				for(int i=0;i<times;i++){
 					variables.push();
 					variables.set("time",Integer.toString(baseTime + plusTime + i * interval));
 					e.calc();
 					variables.pop();
 				}
 			}
 		}
 	}
 	
 	
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Bullet implements Element{
 		private Type type;
 		//private Position position;
 		private Move move;
 		
 		private String[] arguments = new String[7];
 		private String attribute;
 		public Bullet(String attribute){
 			this.attribute = attribute;
 			parse();
 		}
 		public void parse(){
 			String line = null;
 			try{
 				while((line = reader.readLine()) != null && !("".equals(line))){
 					String statement = (new Scanner(line)).next();
 					if(statement.equals("TYPE"))    {type     = new Type(line);}
 					//else if(statement.equals("POSITION")){position = new Position(line);}
 					else if(statement.equals("MOVE"))    {move     = new Move(line);}
 					else if(statement.equals("END")){return;}
 				}
 			}
 			catch(IOException e){
 				System.err.println(e.getMessage());
 			}
 		}
 		
 		
 		
 		public void calc(){
 			int baseTime = parseInt(variables.get("time"));
 			int plusTime = parseInt(attr(attribute,1));
 			int time = baseTime + plusTime;
 			
 			int amount = parseInt(attr(attribute,2));
 			/* inst_type */
 			arguments[0] = "bullet";
 			/* enemy_ID */
 			arguments[1] = Integer.toString(enemyID);
 			type.calc();
 			//position.calc();
 			for(int i=0;i<amount;i++){
 				/* bullet_ID */
 				arguments[2] = Integer.toString(bulletID + i);
 				Instruction instruction = new Instruction(arguments);
 				addInstruction(time,instruction);
 			}
 			variables.push();
 			variables.set("time",Integer.toString(time));
 			variables.set("target_ID",Integer.toString(bulletID));
 			variables.set("amount",Integer.toString(amount));
 			variables.set("target_type","bullet");
 			move.calc();
 			variables.pop();
 			
 			bulletID += amount;
 		}
 		
 		
 		
 		private class Type{
 			private String attribute;
 			public Type(String attribute){
 				this.attribute = attribute;
 			}
 			public void calc(){
 				/* bullet_type*/
 				String type = attr(attribute,1);
 				arguments[3] = type;
 				if(type.equals("custom")){
 					/* image_ID */
 					arguments[4] = attr(attribute,2);
 					/* radius */
 					arguments[5] = attr(attribute,3);
 					/* Power */
 					arguments[6] = attr(attribute,4);
 				}
 			}
 		}
 		private class Position{
 			private String attribute;
 			public Position(String attribute){
 				this.attribute = attribute;
 			}
 			public void calc(){
 				arguments[3] = attr(attribute,1);
 			}
 		}
 	}
 	
 
 
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Route implements Element{
 		private String attribute;
 		public Route(String attribute){
 			this.attribute = attribute;
 		}
 		
 		
 		
 		public void calc(){
 			
 			String[] arguments = new String[10];
 		
 			int baseTime = parseInt(variables.get("time"));
 			int plusTime = parseInt(attr(attribute,1));
 			int time = baseTime + plusTime;
 			
 			/* inst_type */
 			arguments[0] = "route";
 			
 			/* route_type */
 			boolean isTypeValid = false;
 			String routeType = attr(attribute,2);
 			if(routeType.equals("stop")){
 				isTypeValid = true;
 				arguments[3] = "stop";
 			}
 			else if(routeType.equals("line")){
 				isTypeValid = true;
 				arguments[3] = "linear";
 				/* speed */
 				arguments[4] = attr(attribute,3);
 				/* point_refer */
 				arguments[5] = attr(attribute,4);
 				/* point_offset */
 				arguments[6] = attr(attribute,5);
 				/* point_angle */
 				arguments[7] = attr(attribute,6);
 			}
 			
 			if(isTypeValid){
 				/* target_type */
 				String targetType = variables.get("target_type");
 				if(targetType.equals("enemy")){
 					arguments[1] = "enemy";
 					/* target_ID */
 					arguments[2] = variables.get("target_ID");
 					Instruction instruction = new Instruction(arguments);
 					addInstruction(time,instruction);
 				}
 				else if(targetType.equals("bullet")){
 					arguments[1] = "bullet";
 					/* target_ID */
 					int targetID = parseInt(variables.get("target_ID"));
 					int amount   = parseInt(variables.get("amount"));
 					for(int i=0;i<amount;i++){
 						arguments[2] = Integer.toString(targetID + i);
 						Instruction instruction = new Instruction(arguments);
 						addInstruction(time,instruction);
 					}
 				}
 			}
 		}
 	}
 	
 
 
 ////////////////////////////////////////////////////////////////////////////////
 
 	
 	private class Shape implements Element{
 		private String attribute;
 		public Shape(String attribute){
 			this.attribute = attribute;
 		}
 		
 		
 		
 		public void calc(){
 			String[] arguments = new String[10];
 		
 			/* target_type */
 			String targetType = variables.get("target_type");
 			if(targetType.equals("enemy")){
 				return;
 			}
 			arguments[1] = "bullet";
 			
 			int baseTime = parseInt(variables.get("time"));
 			int plusTime = parseInt(attr(attribute,1));
 			int time = baseTime + plusTime;
 			
 			/* inst_type */
 			arguments[0] = "route";
 			
			
 			/* shape_type */
 			String shapeType = attr(attribute,2);
 			if(shapeType.equals("fan")){
 				/* route_type */
 				arguments[3] = "linear";
 				/* speed */
 				arguments[4] = attr(attribute,3);
 				/* point_refer */
 				arguments[5] = attr(attribute,4);
 				/* point_offset */
 				arguments[6] = attr(attribute,5);
 				/* target_ID */
 				int targetID = parseInt(variables.get("target_ID"));
 				
 				int offsetAngle = parseInt(attr(attribute,6));
 				int amount   = parseInt(variables.get("amount"));
 				if(amount == 1){
 					/* target_ID */
 					arguments[2] = Integer.toString(targetID);
 					/* point_angle */
 					arguments[7] = Integer.toString(offsetAngle);
 					Instruction instruction = new Instruction(arguments);
 					addInstruction(time,instruction);
 				}
 				else{
 					int angle = parseInt(attr(attribute,7));
 					float theta;
 					if(angle == 360)
 						theta = angle / (amount);
 					else
 						theta = angle / (amount - 1);
 					float startAngle = offsetAngle - theta * (amount - 1) / 2;
 					for(int i=0;i<amount;i++){
 						/* target_ID */
 						arguments[2] = Integer.toString(targetID + i);
 						/* point_angle */
 						arguments[7] = Integer.toString((int)(startAngle + i * theta));
 						Instruction instruction = new Instruction(arguments);
 						addInstruction(time,instruction);
 					}
 				}
 			}
 			else if(shapeType.equals("spin")){
 				/* route_type */
 				arguments[3] = "linear";
 				/* speed */
 				arguments[4] = attr(attribute,3);
 				/* point_refer */
 				arguments[5] = attr(attribute,4);
 				/* point_offset */
 				arguments[6] = attr(attribute,5);
 				/* target_ID */
 				int targetID = parseInt(variables.get("target_ID"));
 				
 				int offsetAngle = parseInt(attr(attribute,6));
 				int amount   = parseInt(variables.get("amount"));
 				if(amount == 1){
 					/* target_ID */
 					arguments[2] = Integer.toString(targetID);
 					/* point_angle */
 					arguments[7] = Integer.toString(offsetAngle);
 					Instruction instruction = new Instruction(arguments);
 					addInstruction(time,instruction);
 				}
 				else{
 					int angle = parseInt(attr(attribute,7));
 					float theta;
 					if(angle == 360)
 						theta = angle / (amount);
 					else
 						theta = angle / (amount - 1);
 					int interval = parseInt(attr(attribute,8));
 					for(int i=0;i<amount;i++){
 						/* target_ID */
 						arguments[2] = Integer.toString(targetID + i);
 						/* point_angle */
 						arguments[7] = Integer.toString((int)(offsetAngle + i * theta));
 						Instruction instruction = new Instruction(arguments);
 						addInstruction(time + i * interval,instruction);
 					}
 				}
 			}
 		}
 	}
 	
 
 
 ////////////////////////////////////////////////////////////////////////////////	
 
 	
 	private int parseInt(String i){return Integer.valueOf(i);}
 	private String attr(String s,int i){
		return s.split(" +")[i];
 	}
 }
 
 
 class VariableTable{
 	private Stack<HashMap<String,String>> stack = new Stack<HashMap<String,String>>();
 
 	public VariableTable(){
 		push();
 	}
 	public void push(){
 		stack.push(new HashMap<String,String>());
 	}
 	public void pop(){
 		stack.pop();
 		if(stack.empty()){
 			push();
 		}
 	}
 	public void set(String key,String value){
 		stack.peek().put(key,value);
 	}
 	public String get(String key){
 		String out = stack.peek().get(key);
 		if( out != null){
 			return out;
 		}
 		else{
 			int size = stack.size() - 2;
 			for(int i=size;i>=0;i--){
 				out = stack.get(i).get(key);
 				if( out != null){
 					return out;
 				}
 			}
 		}
 		return null;
 	}
 }
