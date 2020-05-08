 import java.io.*;
 import java.util.*;
 import java.awt.*;
 import javax.imageio.ImageIO;
 import java.lang.Math.*;
 
 public class Stage{
 	Image[] images;
 	Instruction[][] instructions;
 	Map<String,Integer> anchors;
 	
 	int PC = 0;
 	
 	public Stage(String stageName){
 		String path = "map/" + stageName + "/";
 		ImageMapper mapper = new ImageMapper(path);
 		images = mapper.get();
 
 		InstructionParser parser = new InstructionParser(path);
 		instructions = (Instruction[][])parser.get();
 	}
 	public Instruction[] get(){
 		if(PC < instructions.length){
 			return instructions[PC++];
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
 	public Image getImage(int imageId){
 		return images[imageId];
 	}
 }
 
 class ImageMapper{
 	String path;
 	BufferedReader reader;
 	public ImageMapper(String path){
 		images = new ArrayList<Image>();
 		this.path = path;
 		try{
 			reader = new BufferedReader(new FileReader(path+"map"));
 			mapImages(reader);
 		}
 		catch(FileNotFoundException e){
 			System.err.println("File not found");
 		}
 	}
 	public Image[] get(){
 		Object[] objs = images.toArray();
 		int length = objs.length;
 		Image[] out = new Image[length];
 		for(int i=0;i<length;i++){
 			out[i] = (Image)objs[i];
 		}
 		
 		/*dump*/
 		/*
 		int len = out.length;
 		for(int i=0;i<len;i++){
 			if(out[i] != null)
 				System.out.println(i+"\t"+out[i].getHeight(null));
 		}
 		*/
 		return out;
 	}
 	private ArrayList<Image> images;
 	private Image background;
 	private void mapImages(BufferedReader reader){
 		String line = null;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				if(!tokens.hasNext())continue;
 				String statement = tokens.next();
 				if(statement.equals("IMAGE")){
 					stateImage();
 				}
 			}
 		}
 		catch(IOException e){
 			System.err.println("Error statement:"+line);
 			return;
 		}
 	}
 	
 	private void stateImage(){
 		String imagePath = path + "image/";
 		String line = null;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				String statement = tokens.next();
 				try{
 					if(statement.equals("IMG")){
 						int imageId = tokens.nextInt();
 						String imageName = tokens.next();
 						File file = new File(imagePath + imageName);
 						Image img = ImageIO.read(file);
 						
 						int size = images.size();
 						for(int i=size;i<imageId;i++)
 							images.add(null);
 						images.add(imageId,img);
 					}
 					else if(statement.equals("BG")){
 						String imageName = tokens.next();
 						File file = new File(imagePath + imageName);
 						background = ImageIO.read(file);
 					}
 					else if(statement.equals("END")){
 						return;
 					}
 				}
 				catch(IOException e){
 					System.out.println("Image can't read");
 				}
 			}
 		}
 		catch(IOException e){
 			System.out.println("Map file can't read");
 			return;
 		}
 	}
 }
 
 class InstructionParser{
 	ArrayList<ArrayList<Instruction>> instructions;
 	Map<String,Integer> anchors;
 	BufferedReader reader;
 	
 	int enemyId = 0;
 	int bulletId = 0;
 	int wave = 0;
 	
 	public InstructionParser(String path){
 		instructions = new ArrayList<ArrayList<Instruction>>();
 		try{
 			reader = new BufferedReader(new FileReader(path + "map"));
 			parseInstruction();
 		}
 		catch(FileNotFoundException e){
 			System.err.println("File not found");
 		}
 	}
 	public Instruction[][] get(){
 		int length = instructions.size();
 		Instruction[][] out = new Instruction[length][];
 		for(int i=0;i<length;i++){
 			ArrayList<Instruction> array = instructions.get(i);
 			if(array != null){
 				Object[] arr = array.toArray();
 				int len = arr.length;
 				out[i] = new Instruction[len];
 				for(int j=0;j<len;j++){
 					out[i][j] = (Instruction)arr[j];
 				}
 			}
 		}
 		
 		/*dump*/
 		/*
 		int len = out.length;
 		for(int i=0;i<len;i++){
 			if(out[i] != null){
 				int slen = out[i].length;
 				System.out.println(i+":");
 				for(int j=0;j<slen;j++){
 					Instruction inst = out[i][j];
 					if(inst.getInstType().equals("enemy")){
 						System.out.println("\t"+inst.getInstType()+"\t"+inst.getEnemyId());
 					}
 					if(inst.getInstType().equals("bullet")){
 						System.out.println("\t"+inst.getInstType()+"\t"+inst.getEnemyId()+"\t"+inst.getBulletId());
 					}
 					if(inst.getInstType().equals("route")){
 						System.out.println("\t"+inst.getInstType()+"\t"+inst.getTargetType()+"\t"+inst.getTargetId());
 					}
 					System.out.println("\t"+inst.getInstType());
 				}
 			}
 		}
 		*/
 		return out;
 	}
 	private void parseInstruction(){
 		String line;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				if(!tokens.hasNext())continue;
 				String statement = tokens.next();
 				if(statement.equals("PARTA")){
 					int time = tokens.nextInt();
 					statePart(time);
 				}
 				else if(statement.equals("PARTB")){
 					int time = tokens.nextInt();
 					statePart(time);
 				}
 				else if(statement.equals("BOSSA")){
 					int time = tokens.nextInt();
 					stateBoss(time);
 				}
 				else if(statement.equals("BOSSB")){
 					int time = tokens.nextInt();
 					stateBoss(time);
 				}
 			}
 		}
 		catch(IOException e){
 			System.out.println("IOException");
 			return;
 		}
 		/*
 		String arguments[] = {"end"};
 		Instruction endInstruction = new Instruction(arguments);
 		ArrayList<Instruction> insts = new ArrayList<Instruction>();
 		insts.add(endInstruction);
 		instructions.add(insts);
 		*/
 	}
 	
 	private void statePart(int baseTime){
 		String line;
 		int time;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				if(!tokens.hasNext())continue;
 				String statement = tokens.next();
 				/*
 				if(statement.equals("GROUP")){
 						time  = tokens.nextInt();
 					int times = tokens.nextInt();
 					int inter = tokens.nextInt();
 					for(int i=0;i<times;i++){
 						stateEnemy(baseTime + time);
 						time += inter;
 					}
 				}
 				
 				else*/
 				if(statement.equals("ENEMY")){
 					time  = tokens.nextInt();
 					stateEnemy(baseTime + time);
 				}
 				else if(statement.equals("END")){
 					return;
 				}
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	private void stateEnemy(int baseTime){
 		String line;
 		int id = enemyId;
 		enemyId++;
 		String[] arguments = new String[7];
 		arguments[0] = "enemy";
 		arguments[1] = Integer.toString(id);
 		
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				if(!tokens.hasNext())continue;
 				String statement = tokens.next();
 				if(statement.equals("TYPE")){
 					String type  = tokens.next();
 					arguments[2] = type;
 					if(type == "custom"){
 						arguments[4] = tokens.next();
 						arguments[5] = tokens.next();
 						arguments[6] = tokens.next();
 					}
 				}
 				else if(statement.equals("POSITION")){
 					arguments[3] = tokens.next();
 					Instruction enemyInstruction = new Instruction(arguments);
 					addInst(baseTime,enemyInstruction);
 				}
 				else if(statement.equals("MOVE")){
 					stateMove(baseTime,"enemy",id,1);
 				}
 				else if(statement.equals("SHOOT")){
 					stateShoot(baseTime,id);
 				}
 				else if(statement.equals("END")){
 					return;
 				}
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	private void stateShoot(int baseTime,int enemyId){
 		String line;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				if(!tokens.hasNext())continue;
 				String statement = tokens.next();
 				if(statement.equals("BULLET")){
 					int time   = tokens.nextInt();
 					int times  = tokens.nextInt();
 					int inter  = tokens.nextInt();
 					int amount = tokens.nextInt();
					stateBullet(baseTime,times,inter,enemyId,amount);
 				}
 				else if(statement.equals("END")){
 					return;
 				}
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	private void stateBullet(int baseTime,int enemyId,int amount){
 		stateBullet(baseTime,1,0,enemyId,amount);
 	}
 	private void stateBullet(int baseTime,int times,int interval,int enemyId,int amount){
 		String line;
 		int id = bulletId;
 		bulletId += times * amount;
 		
 		String[] arguments = new String[7];
 		arguments[0] = "bullet";
 		arguments[1] = Integer.toString(enemyId);
 		
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				if(!tokens.hasNext())continue;
 				String statement = tokens.next();
 				if(statement.equals("TYPE")){
 					String type  = tokens.next();
 					arguments[3] = type;
 					if(type.equals("custom")){
 						arguments[4] = tokens.next();
 						arguments[5] = tokens.next();
 						arguments[6] = tokens.next();
 					}
 					for(int i=0;i<times;i++){
 						for(int j=0;j<amount;j++){
 							arguments[2] = Integer.toString(id + i*amount + j);
 							Instruction bulletInstruction = new Instruction(arguments);
 							addInst(baseTime + i*interval,bulletInstruction);
 						}
 					}
 				}
 				else if(statement.equals("MOVE")){
 					stateMove(baseTime,times,interval,"bullet",id,amount);
 				}
 				else if(statement.equals("END")){
 					return;
 				}
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	private void stateMove(int baseTime,String targetType,int targetId,int amount){
 		stateMove(baseTime,1,0,targetType,targetId,amount);
 	}
 	private void stateMove(int baseTime,int times,int interval,String targetType,int targetId,int amount){
 		String line;
 		String[] arguments = new String[9];
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				String statement = tokens.next();
 			
 				if(statement.equals("ROUTE")){
 					int time = tokens.nextInt();
 					for(int i=0;i<times;i++){
 						stateRoute(baseTime+time + i*interval,targetType,targetId + i*amount,amount,line);
 					}
 				}
 				else if(statement.equals("END")){
 					return;
 				}	
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	private void stateRoute(int baseTime,String targetType,int targetId,int amount,String line){
 		Scanner tokens = new Scanner(line);
 		String statement = tokens.next();
 		int time = tokens.nextInt();
 		
 		String[] arguments = new String[9];
 		arguments[0] = "route";
 		arguments[1] = targetType;
 		//speed
 		arguments[4] = tokens.next();
 
 		String type = tokens.next();
 		if(type.equals("fan")){
 			arguments[3] = "linear";
 			arguments[5] = tokens.next();
 			arguments[6] = tokens.next();
 			
 			int offsetAngle = tokens.nextInt();
 			int angle = tokens.nextInt();
 	
 			if(amount == 1){
 				arguments[2] = Integer.toString(targetId);
 				arguments[7] = "0";
 				Instruction routeInstruction = new Instruction(arguments);
				addInst(baseTime,routeInstruction);
 			}
 			else{
 				float theta = angle/amount;
 				float startAngle = offsetAngle -1 * theta * ((amount - 1)/2);
 				for(int i=0;i<amount;i++){
 					arguments[2] = Integer.toString(targetId + i);
 					arguments[7] = Integer.toString((int)(startAngle + i*theta));
 					Instruction routeInstruction = new Instruction(arguments);
					addInst(baseTime,routeInstruction);
 				}
 			}
 		}
 		else if(type.equals("line")){
 			arguments[3] = "linear";
 			arguments[5] = tokens.next();
 			arguments[6] = tokens.next();
 			
 			int offsetAngle = tokens.nextInt();
 	
 			for(int i=0;i<amount;i++){
 				arguments[2] = Integer.toString(targetId + i);
 				arguments[7] = Integer.toString(offsetAngle);
 				Instruction routeInstruction = new Instruction(arguments);
 				addInst(baseTime,routeInstruction);
 			}
 		}
 		else if(type.equals("stop")){
 			arguments[3] = "stop";
 			arguments[5] = tokens.next();
 			arguments[6] = tokens.next();
 			
 			int offsetAngle = tokens.nextInt();
 	
 			for(int i=0;i<amount;i++){
 				arguments[2] = Integer.toString(targetId + i);
 				arguments[7] = Integer.toString(offsetAngle);
 				Instruction routeInstruction = new Instruction(arguments);
 				addInst(baseTime,routeInstruction);
 			}
 		}
 	}
 	
 	private void stateBoss(int baseTime){
 		int time = 0;
 		int id = enemyId;
 		enemyId++;
 		
 		String[] arguments = new String[7];
 		arguments[0] = "enemy";
 		arguments[1] = Integer.toString(enemyId);
 		arguments[2] = "custom";
 		
 		String line;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				String statement = tokens.next();
 				if(statement.equals("TYPE")){
 					arguments[4] = tokens.next();
 					arguments[5] = tokens.next();
 					}
 				else if(statement.equals("WAVE")){
 					int period = tokens.nextInt();
 					time += period;
 					anchors.put("WAVE"+wave,new Integer(baseTime + time));
 					wave++;
 					arguments[6] = tokens.next();
 				
 					Instruction enemyInstruction = new Instruction(arguments);
 					addInst(baseTime + time,enemyInstruction);
 					Instruction bossInstruction = new Instruction(new String[]{"boss",Integer.toString(period)});
 					addInst(baseTime + time,bossInstruction);
 					stateWave(baseTime,id);
 					}
 				else if(statement.equals("END")){
 					return;
 				}
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	private void stateWave(int baseTime,int id){
 		String[] arguments = new String[9];
 		String line;
 		try{
 			while((line = reader.readLine()) != null && line != ""){
 				Scanner tokens = new Scanner(line);
 				String statement = tokens.next();
 				if(statement.equals("MOVE")){
 					stateMove(baseTime,"enemy",id,1);
 					}
 				else if(statement.equals("BULLET")){
 					int time   = tokens.nextInt();
 					int times  = tokens.nextInt();
 					int inter  = tokens.nextInt();
 					int amount = tokens.nextInt();
 					for(int i=0;i<times;i++){
 						stateBullet(baseTime,id,amount);
 						time += inter;
 					}
 					}
 				else if(statement.equals("END")){
 					return;
 				}
 			}
 		}
 		catch(IOException e){
 			return;
 		}
 	}
 	
 	private void addInst(int time,Instruction inst){
 		int size = instructions.size();
 		int sub  = time - size;
 		for(int i=0;i<=sub;i++){
 			instructions.add(null);
 		}
 		ArrayList<Instruction> list = instructions.get(time);
 		if(list == null){
 			list = new ArrayList<Instruction>();
 			instructions.set(time,list);
 		}
 		list.add(inst);
 	}
 }
 
