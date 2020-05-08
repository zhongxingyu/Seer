 package team.black.fruitswirl;
 
import org.flixel.FlxG;
import org.flixel.FlxSprite;
 import org.flixel.FlxState;
 import org.flixel.FlxText;
 
 public class PlayState extends FlxState
 {
 	@Override
 	public void create()
 	{
		FlxSprite bg = new FlxSprite(0,0);
		bg.makeGraphic(FlxG.width, FlxG.height, 0xFF0000FF);
		add(bg);
		add(new FlxText(0, 0, 200, "Testing Testing...."));
		FlxG.log("Foo");
 	}
 }
