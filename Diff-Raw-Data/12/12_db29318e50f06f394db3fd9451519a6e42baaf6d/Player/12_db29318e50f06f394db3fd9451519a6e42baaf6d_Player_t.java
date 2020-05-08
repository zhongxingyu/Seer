 package game.entidade;
 import java.util.ArrayList;
 import game.interfaces.*;
 
 
 public class Player implements SetandoBonus {
 	
 	public String nome;
 	public String sexo;
 	public ArrayList<Item> mochila = new ArrayList<Item>();
 	public int lvl;
 	public int atk = (this.lvl*4);
 	public int def =  (this.lvl*4);
 	public int hp = (this.atk/2+this.def*2);
 	private int hpMax = (this.atk/2+this.def*2);
 	public int speed = ((this.lvl*2)-this.hp/4);
 	public int jump;
 	public int xp;
 	private int xpMax = (this.lvl*5);
 	public int bonusDef;
 	public int bonusAtk;
 	public int bonusHp;
	public Arma arma = null;
	public Vestimenta roupa = null;
 		
 	public Player() {
 		this.nome = "sem nome";
 		this.sexo = "nao informado";
 		this.lvl = 0;
 		this.xp = 0;
 		setStatus();
 		
 	}
 	
 	public Player(String nomeP, String sexoP, int lvlP){
 		
 		this.nome = nomeP;
 		this.sexo = sexoP;
 		this.lvl = lvlP;
 		this.xp = 0;
 		setStatus();
 		
 	}
 	
 	private void setStatus(){
 		this.atk =  (this.lvl*4);
 		this.def = (this.lvl*2);
 		this.hp = (this.atk/2+this.def*2);
 		this.hpMax = (this.atk/2+this.def*2);
 		this.speed = (int)((this.lvl*3)+(this.atk*0.65)-(this.def*0.4));
 		this.jump = 20;
 		//this.xp = 0;
 		this.xpMax = (this.lvl*5);
		if(arma!= null){
			verificarItem(arma);
		}
		if(roupa!=null){
			verificarItem(roupa);
		}
		
 	}
 	
 	public String getStatus(){
 		return String.format("%s - %s\tLevel: %d\nAtk: %d\t\tDef: %d\n" +
 				"Hp: %d/%d\tSpeed: %d\nJump: %d\tExp: %d/%d\n", this.nome,
 				this.sexo,this.lvl,this.atk,this.def,this.hp, this.hpMax,
 				this.speed,this.jump,this.xp,this.xpMax);
 	}
 	
 	public void addXp(int xp){
 		this.xp += xp;
 		verificarXp();
 	}
 	
 	private void verificarXp(){
 		 while (this.xp >= this.xpMax){
 			 this.xp -= this.xpMax;
 			 int newlvl = this.lvl + 1;
 			 System.out.println("\tLVL UP!!!!!!!\n");
 			 setLvl(newlvl);
 		}
 		 System.out.println(getStatus()+"\n");
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public String getSexo() {
 		return sexo;
 	}
 
 	public void setSexo(String sexo) {
 		this.sexo = sexo;
 	}
 
 	public int getLvl() {
 		return lvl;
 	}
 
 	public void setLvl(int lvl) {
 		this.lvl = lvl;
 		setStatus();
 	}
 
 	public int getAtk() {
 		return atk;
 	}
 
 	public void setAtk(int atk){
 		this.atk= atk;
 	}
 	
 	public int getDef() {
 		return def;
 	}
 
 	public void setDef(int def) {
 		this.def = def;
 	}
 
 	public int getHp() {
 		return hp;
 	}
 
 	public void setHp(int hp) {
 		if(hp<=this.hpMax){
 			this.hp = hp;
 		}
 	}
 
 	public int getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(int speed) {
 		this.speed = speed;
 	}
 	
 	
 	public int getJump() {
 		return jump;
 	}
 
 	public void setJump(int jump) {
 		this.jump = jump;
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public void setXp(int xp) {
 		this.xp = xp;
 	}
 	
 	public ArrayList<Item> getMochila() {
 		return mochila;
 	}
 
 	public void setMochila(ArrayList<Item> mochila) {
 		this.mochila = mochila;
 	}
 
 	public int getHpMax() {
 		return hpMax;
 	}
 	
 	public int getXpMax() {
 		return xpMax;
 	}
 
 	public boolean validaPlayer(String sexo){
 		boolean validacao;
 		
 		String mulher = "mulher";
 		String homem = "homem";
 		
 		if(sexo.equalsIgnoreCase(mulher)){
 			validacao = true;
 		}else if(sexo.equalsIgnoreCase(homem)){
 			validacao = true;
 		}else{
 			System.out.println("Sexo Invalido!");
 			validacao = false;
 		}
 		
 		return validacao;
 	}
 	
 	public void adicionaItem(Item item){
 		if(item instanceof Arma){
 			this.arma = (Arma) item;
 			verificarItem(item);
 		}else if(item instanceof Vestimenta){
 			this.roupa = (Vestimenta) item;
 			verificarItem(item);
 		}else if(item instanceof Consumivel){
 			if (mochila.size()>5){
 				System.out.println("mochila cheia");
 			}else{
 				mochila.add(item);
 			}
 		}
 	}
 	public void verificarItem (Item item){
 		if(item instanceof Arma){
 			 setAtk((this.lvl*4));
 			int atk = item.getBonus()+this.atk;
 			addBonusAtk(atk);
 		}else if(item instanceof Vestimenta){
 			setDef((this.lvl*2));
 			int def = item.getBonus()+this.def;
 			addBonusDef(def);
 		}else{
 			int hpRecuperada = item.getBonus()+this.hp;
 			addBonusHp(hpRecuperada);
 			//Consumivel no adiciona bonus
 		}
 			
 	}
 
 	@Override
 	public void addBonusHp(int hp) {
 		
 		if(hp>this.hpMax){
 			System.out.println("+ " +(this.hpMax-this.hp) + " hp");
 			setHp(this.hpMax);
 		}else if(hp<this.hpMax){
 			System.out.println("+ " +(hp-this.hp) + " hp");
 			setHp(hp);
 		}
 	}
 
 	@Override
 	public void addBonusAtk(int atk) {
 		setAtk(atk);
 	}
 
 	@Override
 	public void addBonusDef(int def) {
 		setDef(def);
 	}
 }
 	 
