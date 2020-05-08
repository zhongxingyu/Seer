 package union.jsbot;
 
 import haven.Button;
 import haven.Widget;
 import haven.Window;
 import union.JSBot;
 import union.JSGUI.JSGUI_Widget;
 
 public class JSGUI_Window extends JSGUI_Widget {
 	public JSGUI_Window(int lid) {
 		super(lid);
 	}
 	
 	/**
 	 * Показать/скрыть кнопку "закрыть" у окна
 	 */
 	public void toggleCloseButton() {
 		((Window)wdg()).cbtn.visible = !((Window)wdg()).cbtn.visible;
 	}
 	
 	/**
 	 * Закрыть окно
 	 */
 	public void close() {
 		((Window)wdg()).cbtn.click();
 	}
 
     /**
      * Ждет нажатия кнопки в окне
      * @return текст нажатой кнопки
      * @param timeout таймаут.
      * Других идей у меня небыло
      */
 	public String waitButtonClick(int timeout) {
        if (timeout == 0){
            timeout = 10000;
        }
         int cur = 0;
 		while(cur <= timeout){
 			for(Widget i = wdg().child; i != null; i = i.next) {
 				if(i instanceof Button){
 					Button b = (Button) i;
 					if(b.isChanged()) return b.text.text;
 				}
 			}
			if (!JSBot.Sleep(25)) break;
            cur += 25;
 		}
 		return "";
 	}
 	
 	/**
 	 * Перегрузка для уже имеющихся скриптов
 	 */
 	public String waitButtonClick() {
 		return waitButtonClick(0);
 	}
 }
