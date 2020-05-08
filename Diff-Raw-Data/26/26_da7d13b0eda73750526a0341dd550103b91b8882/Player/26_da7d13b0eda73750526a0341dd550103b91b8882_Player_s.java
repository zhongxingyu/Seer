 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.engine;
 
 import com.trollsahead.qcumberless.device.Device;
 import com.trollsahead.qcumberless.device.DeviceCallback;
 import com.trollsahead.qcumberless.gui.elements.*;
 import com.trollsahead.qcumberless.model.PlayResult;
 import com.trollsahead.qcumberless.util.HistoryHelper;
 import com.trollsahead.qcumberless.model.Screenshot;
 import com.trollsahead.qcumberless.util.ElementHelper;
 
 import java.awt.*;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.List;
 
 public class Player implements DeviceCallback {
     private static final Set<Color> PLAYING_COLORS = new HashSet<Color>();
 
     private static final long MESSAGE_TIMEOUT_PLAYER = 5L * 1000L;
     private static final long MESSAGE_ANIMATION_SPEED = 300;
     private static final int MESSAGE_WIDTH = 200;
     private static final int MESSAGE_HEIGHT = 25;
 
     private static final String TEXT_SUCCESS = "SUCCESS!";
     private static final String TEXT_FAILURE = "FAILURE!";
 
     private static final Color COLOR_SUCCESS = new Color(0x88FF88);
     private static final Color COLOR_FAILURE = new Color(0x990000);
 
     public static long messageTimeout = 0;
 
     public BaseBarElement currentFeature = null;
     public BaseBarElement currentScenario = null;
     public BaseBarElement currentBackground = null;
     public BaseBarElement currentStep = null;
     public ExamplesElement currentExamples = null;
 
     public int currentExamplesRow = -1;
     private int currentStepIndex;
 
     private boolean didFinishBackground;
 
     public boolean started;
     public boolean running;
     public boolean paused;
     public boolean stopped;
 
     private static boolean notifiedStopped;
 
     private boolean success;
 
     private Device device;
 
     private Color runningColor = Color.GRAY;
 
     private static boolean hasDeviceFailures = false;
 
     public static Set<Player> players = new HashSet<Player>();
 
     static {
         PLAYING_COLORS.add(Color.WHITE);
         PLAYING_COLORS.add(Color.YELLOW);
         PLAYING_COLORS.add(Color.PINK);
     }
     
     public static void prepareRun() {
         hasDeviceFailures = false;
         notifiedStopped = false;
         DesignerEngine.featuresRoot.setPlayStateIncludingChildren(PlayResult.State.NOT_PLAYED);
         DesignerEngine.setColorScheme(Element.ColorScheme.PLAY);
     }
 
     public Player() {
         runningColor = getUnusedPlayingColor();
         players.add(this);
         reset();
     }
 
     protected void finalize() throws Throwable {
         cleanup();
         super.finalize();
     }
     
     private void cleanup() {
         players.remove(this);
     }
 
     public static boolean isStarted() {
         for (Player player : players) {
             if (player.started) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isRunning() {
         for (Player player : players) {
             if (player.running) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isPaused() {
         for (Player player : players) {
             if (player.paused) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isStopped() {
         for (Player player : players) {
             if (player.stopped) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isNotifiedStopped() {
         return notifiedStopped;
     }
 
     public void play(final List<BaseBarElement> features, final Device device, final Set<String> tags) {
         this.device = device;
         device.setDeviceCallback(this);
         device.getConsoleOutput().clearLog();
         new Thread(new Runnable() {
             public void run() {
                success = true;
                started = true;
                long startTime = System.currentTimeMillis();
                device.play(FeatureBuilder.buildFeatures(features), tags);
                HistoryHelper.saveRunOutcome(device, features, startTime);
                cleanup();
             }
         }).start();
     }
 
     public static void pause() {
         for (final Player player : players) {
             new Thread(new Runnable() {
                 public void run() {
                     player.device.pause();
                 }
             }).start();
         }
     }
 
     public static void resume() {
         for (final Player player : players) {
             new Thread(new Runnable() {
                 public void run() {
                     player.device.resume();
                 }
             }).start();
         }
     }
 
     public static void stop() {
         notifiedStopped = true;
         for (final Player player : players) {
             new Thread(new Runnable() {
                 public void run() {
                     player.device.stop();
                 }
             }).start();
         }
     }
 
     private void reset() {
         running = false;
         stopped = false;
         resetCurrentScenario();
         currentFeature = null;
     }
 
     private static boolean hasFailures() {
         for (Player player : players) {
             if (!player.success) {
                 return true;
             }
         }
         return false;
     }
 
     public static void render(Graphics g) {
         if (System.currentTimeMillis() > messageTimeout) {
             return;
         }
 
         String text = hasDeviceFailures ? TEXT_FAILURE : TEXT_SUCCESS;
 
         final int WIDTH = MESSAGE_WIDTH;
         final int HEIGHT = MESSAGE_HEIGHT;
 
         int x = (Engine.windowWidth - WIDTH) / 2;
         int y = 20;
         
         int textX = x + (WIDTH - Engine.fontMetrics.stringWidth(text)) / 2;
         int textY = y + (HEIGHT - Engine.fontMetrics.getHeight()) / 2 - 3;
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.3f));
         g.fillRoundRect(x, y, WIDTH, HEIGHT, 10, 10);
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
         g.drawRoundRect(x, y, WIDTH, HEIGHT, 10, 10);
 
         if (System.currentTimeMillis() % (MESSAGE_ANIMATION_SPEED * 2) < MESSAGE_ANIMATION_SPEED) {
             g.setColor(hasDeviceFailures ? COLOR_FAILURE : COLOR_SUCCESS);
         } else {
             return;
         }
         g.drawString(text, textX, textY + Engine.fontMetrics.getHeight());
     }
 
     public void onPlay() {
         success = true;
         DesignerEngine.cucumberRoot.clearRunStatus();
     }
 
     public void onPause() {
         paused = true;
     }
 
     public void onResume() {
         paused = false;
     }
 
     public void onStop() {
         stopped = true;
     }
 
     public void afterPlayed() {
         setSuccess(currentStep);
         setSuccess(currentExamplesRow);
         reset();
         messageTimeout = System.currentTimeMillis() + MESSAGE_TIMEOUT_PLAYER;
     }
 
     public void afterPlayFailed(String errorMessage) {
         failure();
         reset();
         messageTimeout = System.currentTimeMillis() + MESSAGE_TIMEOUT_PLAYER;
     }
 
     public void logLine(String line) {
         device.getConsoleOutput().appendLog(line, getCurrentLoglineElement());
     }
 
     public void beforeFeatures() {
         running = true;
     }
 
     public void beforeFeature(String name) {
         setSuccess(currentStep);
         setSuccess(currentExamplesRow);
         resetCurrentScenario();
         currentFeature = (BaseBarElement) DesignerEngine.featuresRoot.findChild(name);
         setSuccess(currentFeature);
     }
 
     public void beforeScenario(String name) {
         setSuccess(currentStep);
         setSuccess(currentExamplesRow);
         resetCurrentStep();
         if (currentFeature != null) {
             currentScenario = (BaseBarElement) currentFeature.findChild(name);
             currentStep = (BaseBarElement) currentFeature.firstChildOfType(BaseBarElement.TYPE_STEP);
             currentStepIndex = currentFeature.findChildIndex(currentStep);
             setSuccess(currentScenario);
         } else {
             resetCurrentScenario();
         }
         didFinishBackground = false;
     }
 
     public void beforeBackground(String name) {
     }
 
     public void beforeStep(String name) {
         setSuccess(currentStep);
         BaseBarElement scenarioOrBackground = currentScenario;
         if (!didFinishBackground) {
             BaseBarElement backgroundElement = ElementHelper.findBackgroundElement(currentFeature);
             if (backgroundElement != null) {
                 setSuccess(currentBackground);
                 currentBackground = backgroundElement;
                 scenarioOrBackground = backgroundElement;
             }
         }
         currentStep = scenarioOrBackground != null ? (BaseBarElement) scenarioOrBackground.findChildFromIndex(name, currentStepIndex + 1) : null;
         if (!didFinishBackground && currentStep == null) {
             currentBackground = null;
             didFinishBackground = true;
             currentStepIndex = -1;
             scenarioOrBackground = currentScenario;
             currentStep = scenarioOrBackground != null ? (BaseBarElement) scenarioOrBackground.findChildFromIndex(name, currentStepIndex + 1) : null;
         }
         currentStepIndex = scenarioOrBackground != null ? scenarioOrBackground.findChildIndex(currentStep) : -1;
     }
 
     public void beforeOutlineTable() {
         if (!(currentScenario instanceof ScenarioOutlineElement)) {
             return;
         }
         resetCurrentStep();
         currentExamples = ((ScenarioOutlineElement) currentScenario).getExamplesElement();
         currentExamplesRow = -1;
     }
 
     public void beforeTableRow(String tableRow) {
         if (currentExamples == null) {
             return;
         }
         setSuccess(currentExamplesRow);
         currentExamplesRow++;
     }
 
     public void afterStepFailed(String errorMessage) {
         failure(errorMessage);
     }
 
     public void attachScreenshots(Element element, Screenshot... screenshots) {
         ((BaseBarElement) element).getPlayResult().setScreenshots(screenshots);
     }
 
     public Element getCurrentElement() {
         return currentStep != null ? currentStep : null;
     }
 
     private void failure() {
         setFailed();
     }
     
     private void failure(String errorMessage) {
         setFailed();
         if (currentExamples != null && currentExamplesRow != -1) {
             currentExamples.setFailed(currentExamplesRow, errorMessage);
         }
         if (currentStep != null && errorMessage != null) {
             currentStep.setFailed();
             currentStep.setErrorMessage(errorMessage);
         }
         if (currentScenario != null) {
             currentScenario.setFailed();
         }
         if (currentFeature != null) {
             currentFeature.setFailed();
         }
     }
 
     private void setFailed() {
         success = false;
         hasDeviceFailures = true;
         DesignerEngine.buttonBar.setFailed();
     }
 
     private void resetCurrentScenario() {
         resetCurrentStep();
         currentScenario = null;
         currentBackground = null;
         didFinishBackground = false;
     }
 
     private void resetCurrentStep() {
         currentExamples = null;
         currentExamplesRow = -1;
         currentStep = null;
         currentStepIndex = -1;
     }
 
     private boolean isRunningElement(BaseBarElement element) {
         return  currentFeature == element ||
                 currentBackground == element ||
                 currentScenario == element ||
                 currentStep == element ||
                 currentExamples == element;
     }
 
     public static boolean isElementRunning(BaseBarElement element) {
         for (Player player : players) {
             if (player.isRunningElement(element)) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isCurrentFeature(BaseBarElement element) {
         for (Player player : players) {
             if (player.currentFeature == element) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isCurrentScenario(BaseBarElement element) {
         for (Player player : players) {
             if (player.currentScenario == element) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isCurrentBackground(BaseBarElement element) {
         for (Player player : players) {
             if (player.currentBackground == element) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isCurrentStep(BaseBarElement element) {
         for (Player player : players) {
             if (player.currentStep == element && !(player.currentScenario instanceof ScenarioOutlineElement)) {
                 return true;
             }
         }
         return false;
     }
 
     public static Color getPlayingColor(BaseBarElement element) {
         for (Player player : players) {
             if (player.isRunningElement(element)) {
                 return player.runningColor;
             }
         }
         return Color.GRAY;
     }
 
     public static Color getPlayingColor(Device device) {
         for (Player player : players) {
             if (player.device == device) {
                 return player.runningColor;
             }
         }
         return Color.GRAY;
     }
 
     public static boolean isDeviceRunning(Device device) {
         for (Player player : players) {
             if (player.device == device) {
                 return player.running;
             }
         }
         return false;
     }
 
     public static boolean isDeviceStarted(Device device) {
         for (Player player : players) {
             if (player.device == device) {
                 return player.started;
             }
         }
         return false;
     }
 
     private static Color getUnusedPlayingColor() {
         Set<Color> colors = new HashSet<Color>();
         colors.addAll(PLAYING_COLORS);
         for (Player player : players) {
             colors.remove(player.runningColor);
         }
         if (colors.size() > 0) {
             return colors.iterator().next();
         } else {
             return PLAYING_COLORS.iterator().next();
         }
     }
 
     public static boolean isPausable() {
         for (Player player : players) {
             if (player.device.getCapabilities().contains(Device.Capability.PAUSE)) {
                 return true;
             }
         }
         return false;
     }
     
     private void setSuccess(BaseBarElement element) {
         if ((element instanceof StepElement) && ((currentScenario != null && currentScenario.getPlayResult().isFailed()) || ((currentScenario instanceof ScenarioOutlineElement) && currentExamples == null))) {
             return;
         }
         if (element != null && !element.getPlayResult().isFailed()) {
             element.setSuccess();
         }
     }
 
     private void setSuccess(int examplesRow) {
         if (currentExamples != null && examplesRow > 0 && !currentExamples.isFailed(examplesRow)) {
             currentExamples.setSuccess(examplesRow);
         }
     }
 
     public static boolean isPlayingExampleRow(BaseBarElement element, int row) {
         if (!(element instanceof ExamplesElement)) {
             return false;
         }
         ExamplesElement examplesElement = (ExamplesElement) element;
         if (!isCurrentScenario((BaseBarElement) examplesElement.groupParent)) {
             return false;
         }
         for (Player player : players) {
             if (player.currentExamples == examplesElement && player.currentExamplesRow == row) {
                 return true;
             }
         }
         return false;
     }
 
     public static List<Device> getStartedDevices() {
         List<Device> devices = new LinkedList<Device>();
         for (Player player : players) {
             devices.add(player.device);
         }
         return devices;
     }
 
     private BaseBarElement getCurrentLoglineElement() {
         return currentExamples != null ? currentExamples : currentStep;
     }
 }
