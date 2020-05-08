 /*
  * Kurento Android MSControl: MSControl implementation for Android.
  * Copyright (C) 2011  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.kas.mscontrol;
 
 import com.kurento.commons.mscontrol.Configuration;
 import com.kurento.commons.mscontrol.MediaSession;
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.Parameter;
 import com.kurento.commons.mscontrol.Parameters;
 import com.kurento.commons.mscontrol.mediacomponent.MediaComponent;
 import com.kurento.kas.mscontrol.mediacomponent.MediaComponentAndroid;
 
 /**
  * MediaSessionAndroid is an interface that extends MediaSession.<br>
  * 
  * Defines a set of parameters to config a MediaSession instance.
  * 
  */
 public interface MediaSessionAndroid extends MediaSession {
 
 
 	/**
 	 * STUN_HOST to indicate the stun server name
 	 */
 	public static final Parameter STUN_HOST = new Parameter() {
 	};
 	
 
 	/**
 	 * STUN_PORT to indicate the stun server port
 	 */
 	public static final Parameter STUN_PORT = new Parameter() {
 	};
 	
 	/**
 	 * MediaSessionAndoid.NetIF to indicate if the network interface is WIFI or
 	 * MOBILE.
 	 */
 	public static final Parameter NET_IF = new Parameter() {
 	};
 
 	/**
 	 * InetAddress to indicate the local IP address
 	 */
 	public static final Parameter LOCAL_ADDRESS = new Parameter() {
 	};
 	
 	/**
 	 * InetAddress to indicate the public IP address
 	 */
 	public static final Parameter PUBLIC_ADDRESS = new Parameter() {
 	};
 
 	/**
 	 * Integer to indicate the max bandwidth will be used in bps(bits per
 	 * second).
 	 */
 	public static final Parameter MAX_BANDWIDTH = new Parameter() {
 	};
 
 	/**
 	 * HashMap&lt;MediaType, Mode&gt; to indicate the mode of each media stream.
 	 */
 	public static final Parameter STREAMS_MODES = new Parameter() {
 	};
 
 	/**
 	 * ArrayList&lt;AudioCodecType&gt; to indicate the audio codecs supported.
 	 */
 	public static final Parameter AUDIO_CODECS = new Parameter() {
 	};
 
 	/**
 	 * ArrayList&lt;VideoCodecType&gt; to indicate the video codecs supported.
 	 */
 	public static final Parameter VIDEO_CODECS = new Parameter() {
 	};
 
 	/**
 	 * Integer to indicate the frame width in pixels.
 	 */
 	public static final Parameter FRAME_WIDTH = new Parameter() {
 	};
 	
 	/**
	 * Integer to indicate the frame heigth in pixels.
 	 */
	public static final Parameter FRAME_HEIGTH = new Parameter() {
 	};
 
 	/**
 	 * Integer to indicate the max frame rate will be used.
 	 */
 	public static final Parameter MAX_FRAME_RATE = new Parameter() {
 	};
 	/**
 	 * Integer to indicate the max number of frames in a group of pictures, or 0
 	 * for intra_only.
 	 */
 	public static final Parameter GOP_SIZE = new Parameter() {
 	};
 
 	/**
 	 * Integer to indicate the number of frames will be buffered from the
 	 * camera.
 	 */
 	public static final Parameter FRAMES_QUEUE_SIZE = new Parameter() {
 	};
 
 	/**
 	 * Create a MediaComponentAndroid.
 	 * 
 	 * @param predefinedConfig
 	 *            Defines the concrete MediaComponentAndroid to create. <br>
 	 *            Predefined Configurations are:
 	 *            <ul>
 	 *            <li>{@link MediaComponentAndroid.AUDIO_PLAYER}: create a
 	 *            component that record audio from microphone.
 	 *            <li>{@link MediaComponentAndroid.AUDIO_RECORDER}: create a
 	 *            component that play audio.
 	 *            <li>{@link MediaComponentAndroid.VIDEO_PLAYER}: create a
 	 *            component that record video from camera.
 	 *            <li>{@link MediaComponentAndroid.VIDEO_RECORDER}: create a
 	 *            component that show video in a display.
 	 *            </ul>
 	 * @param params
 	 *            Parameters to create a MediaComponent. Each concrete component
 	 *            has its own parameters:
 	 * 
 	 *            <table border="1" cellpadding=5>
 	 *            <tr>
 	 *            <thead>
 	 *            <th>Configurarion&lt;MediaComponentAndroid&gt;</th>
 	 *            <th colspan=3>Parameters</th>
 	 *            </tr>
 	 * 
 	 *            <tr>
 	 *            <th></th>
 	 *            <th>Parameter</th>
 	 *            <th>Value type</th>
 	 *            <th>Description</th>
 	 *            </tr>
 	 *            </thead> <tbody>
 	 * 
 	 *            <tr>
 	 *            <td>{@link MediaComponentAndroid.AUDIO_PLAYER}</td>
 	 *            <td colspan=3>{@link Parameters.NO_PARAMETER}</td>
 	 *            </tr>
 	 * 
 	 *            <tr>
 	 *            <td>{@link MediaComponentAndroid.AUDIO_RECORDER}</td>
 	 *            <td>{@link MediaComponentAndroid.STREAM_TYPE}</td>
 	 *            <td>int (see android.media.AudioManager)</td>
 	 *            <td>Speaker that will be used.</td>
 	 *            </tr>
 	 * 
 	 *            <tr>
 	 *            <td>{@link MediaComponentAndroid.VIDEO_PLAYER}</td>
 	 *            <td>{@link MediaComponentAndroid.PREVIEW_SURFACE}</td>
 	 *            <td>android.view.View</td>
 	 *            <td>Surface in which the preview images from the camera will
 	 *            be previewed</td>
 	 *            </tr>
 	 * 
 	 *            <tr>
 	 *            <td rowspan=4 valign=top>
 	 *            {@link MediaComponentAndroid.VIDEO_RECORDER}</td>
 	 *            <td>{@link MediaComponentAndroid.VIEW_SURFACE}</td>
 	 *            <td>android.view.View</td>
 	 *            <td>Surface in which the video receibed will be played.</td>
 	 *            </tr>
 	 *            <tr>
 	 *            <td>{@link MediaComponentAndroid.DISPLAY_WIDTH}</td>
 	 *            <td>int</td>
 	 *            <td>Width of surface designated by
 	 *            {@link MediaComponentAndroid.VIEW_SURFACE}</td>
 	 *            </tr>
 	 *            <tr>
 	 *            <td>{@link MediaComponentAndroid.DISPLAY_HEIGHT}</td>
 	 *            <td>int</td>
 	 *            <td>Height of surface designated by
 	 *            {@link MediaComponentAndroid.VIEW_SURFACE}</td>
 	 *            </tr>
 	 *            <tr>
 	 *            <td>{@link MediaComponentAndroid.DISPLAY_ORIENTATION}</td>
 	 *            <td>int</td>
 	 *            <td>Orientation in which the video will be played.</td>
 	 *            </tr>
 	 * 
 	 * 
 	 *            </tbody>
 	 *            </table>
 	 * 
 	 * @return a MediaComponentAndroid
 	 * @throws MsControlException
 	 */
 	@Override
 	public MediaComponentAndroid createMediaComponent(
 			Configuration<MediaComponent> predefinedConfig, Parameters params)
 			throws MsControlException;
 
 }
