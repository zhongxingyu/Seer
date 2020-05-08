 package com.cvgstudios.pokemonchrome.javascript;
 
 import com.cvgstudios.pokemonchrome.gamelogic.PokemonType;
 
 public class JSPokemonTypeHandler {
 
 	PokemonType pokemonType;
 
 	public JSPokemonTypeHandler(PokemonType pokemon) {
 		pokemonType = pokemon;
 	}
 
 	public void set_type_name(String name) {
 		pokemonType.setTypeName(name);
 	}
 
 	public void set_base_hp(int hp) {
 		pokemonType.setHP(hp);
 	}
 
 	public void set_base_attack(int attack) {
 		pokemonType.setAttack(attack);
 	}
 
 	public void set_base_defense(int defense) {
 		pokemonType.setDefense(defense);
 	}
 
 	public void set_base_sp_attack(int spAttack) {
 		pokemonType.setSpAttack(spAttack);
 	}
 
 	public void set_base_sp_defense(int spDefense) {
 		pokemonType.setSpDefense(spDefense);
 	}
 
 	public void set_base_speed(int speed) {
 		pokemonType.setSpeed(speed);
 	}
 
 	public void register_image_front(String imagepath) {
 		pokemonType.setFrontImage(imagepath);
 	}
 
 	public void register_image_back(String imagepath) {
 		pokemonType.setBackImage(imagepath);
 	}
 
 	public void register_action(String actionname, String actionTrueName) {
 		pokemonType.giveAction(actionname, actionTrueName);
 	}
 
 }
