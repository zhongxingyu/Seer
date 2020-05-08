 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtvprog;
 
 /**
  * Release TV program to Ribbon system.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class RibbonReleaser {
     
     /**
      * Releaser configuration properties.
      */
     private java.util.Properties releaseProps;
     
     /**
      * Channel release flag.
      */
     private Boolean channelRelease;
     
     /**
      * Day release flag.
      */
     private Boolean dayRelease;
     
     /**
      * Allow incomplete relese.
      */
     private Boolean allowIncomplete;
     
     /**
      * Status of execution.
      */
     private String status;
     
     /**
      * Default constructor.
      */
     public RibbonReleaser() {
         releaseProps = JTVProg.tvApp.getProperties("Release.properties", JTVProg.class.getResourceAsStream("Release.properties"));
         this.channelRelease = Boolean.parseBoolean(releaseProps.getProperty("release_channel"));
         this.dayRelease = Boolean.parseBoolean(releaseProps.getProperty("release_day"));
         this.allowIncomplete = Boolean.parseBoolean(releaseProps.getProperty("release_aloow_incomplete"));
     }
     
     /**
      * Render message for the user.
      * @return formatted message;
      */
     public String renderMessage() {
         return "Параметры выпуска:\n"
                 + (this.channelRelease ? "Выпуск каналов разрешен: " + this.releaseProps.getProperty("release_chn_dir") + "\n" : "Выпуск каналов отключен\n") 
                 + (this.dayRelease ? "Выпуск по дням разрешен: " + this.releaseProps.getProperty("release_day_dir") + "\n" : "Выпуск по дням отключен\n")
                 + (this.allowIncomplete ? "Неполный выпуск разрешен." : "Неполный выпуск запрещен!");
     }
     
     /**
      * Iterate through channels and days and release them to the system.
      */
     public void release() {
         if (!this.allowIncomplete && JTVProg.isPass()) {
             this.status = "Невозможно выпустить: пропущены каналы!";
             return;
         }
         if (this.channelRelease) {
            for (int chIndex = 1; chIndex < JTVProg.configer.ChannelProcessor.getSetSize() + 1; chIndex++) {
                 jtvprog.chProcSet.chProcUnit currChannel = JTVProg.configer.ChannelProcessor.getUnit(chIndex);
                 if (!currChannel.isPassedNull) {
                     String res = this.release(currChannel.chName, currChannel.chStored, this.releaseProps.getProperty("release_chn_dir"));
                     if (res.contains("RIBBON_ERROR:")) {
                         JTVProg.warningMessage("Ошибка выпуска канала " + currChannel.chName + "\n\nОтвет системы:\n"
                                 + Generic.CsvFormat.parseDoubleStruct(res)[1]);
                         this.status = currChannel.chName + ": " + Generic.CsvFormat.parseDoubleStruct(res)[1];
                         return;
                     }
                 }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
             }
         }
     }
     
     /**
      * Release message to the system (universal).
      * @param header header of the message;
      * @param content content of message;
      * @param dirs dirs to apply;
      */
     private String release(String header, String content, String dirs) {
         MessageClasses.Message currMessage = new MessageClasses.Message(header, 
                 JTVProg.tvApp.CURR_LOGIN, 
                 "RU", 
                 dirs.replaceFirst("\\[", "").replaceFirst("\\]", "").split(","),
                 new String[] {"телепрограмма", "выпуск"},
                 content
                 );
         String command;
         command = "RIBBON_POST_MESSAGE:-1," + Generic.CsvFormat.renderGroup(currMessage.DIRS) + "," + "RU" + ",{" +
         currMessage.HEADER + "}," + Generic.CsvFormat.renderGroup(currMessage.TAGS) + "," + 
         Generic.CsvFormat.renderMessageProperties(currMessage.PROPERTIES) + "\n" + currMessage.CONTENT + "\nEND:";
         return JTVProg.tvApp.appWorker.sendCommandWithReturn(command);
     }
     
     /**
      * Return status of releasing.
      * @return string or null;
      */
     public String getStatus() {
         return this.status;
     }
 }
